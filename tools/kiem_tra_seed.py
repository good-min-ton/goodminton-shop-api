#!/usr/bin/env python3
"""Kiểm tra tính toàn vẹn của dữ liệu seed (V1 + V2 + V3).

Cách làm: nạp thật các file migration vào một PostgreSQL dùng một lần rồi truy
vấn, thay vì bóc SQL bằng regex. Nhờ vậy mọi ràng buộc UNIQUE, CHECK và khoá
ngoại đều được chính PostgreSQL kiểm giùm - một file seed nạp không nổi thì
những phép đếm bên dưới có đúng cũng vô nghĩa.

    python3 tools/kiem_tra_seed.py              # chỉ kiểm tra, in báo cáo
    python3 tools/kiem_tra_seed.py --check-urls # kiểm thêm từng URL ảnh (chậm)
    python3 tools/kiem_tra_seed.py --fix        # vá sản phẩm thiếu ảnh vào V3

Thoát với mã khác 0 khi còn vấn đề, nên dùng được trong CI.
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

GOC = Path(__file__).resolve().parent.parent
THU_MUC_MIGRATION = GOC / "src/main/resources/db/migration"

# Ảnh của seed nằm trên cùng tài khoản Cloudinary này.
ANH_PHAI_NAP = ["V1__init_schema", "V2__create_indexes", "V3__seed_initial_catalog"]

# Cùng image với máy chủ, để hành vi của enum, UNIQUE NULLS và pgvector giống hệt.
IMAGE_PG = "pgvector/pgvector:pg15"
TEN_CONTAINER = "goodminton-kiem-tra-seed"

DAU_KHOI_VA = "-- === ảnh thay thế do tools/kiem_tra_seed.py sinh ra ==="
CUOI_KHOI_VA = "-- === hết khối ảnh thay thế ==="


# ----------------------------------------------------------------- tiện ích


def chay(*lenh: str, dau_vao: bytes | None = None) -> subprocess.CompletedProcess:
    return subprocess.run(lenh, input=dau_vao, capture_output=True)


def psql(sql: str) -> str:
    """Chạy một câu lệnh, trả về kết quả dạng cột phân tách bởi '|'."""
    kq = chay(
        "docker", "exec", "-i", TEN_CONTAINER,
        "psql", "-U", "postgres", "-d", "kt", "-tAF|", "-c", sql,
    )
    if kq.returncode != 0:
        raise SystemExit(f"psql lỗi:\n{kq.stderr.decode()}")
    return kq.stdout.decode()


def hang(sql: str) -> list[list[str]]:
    return [d.split("|") for d in psql(sql).strip().splitlines() if d]


def mot_o(sql: str) -> str:
    return psql(sql).strip()


# ------------------------------------------------------- dựng cơ sở dữ liệu


def dung_db() -> None:
    chay("docker", "rm", "-f", TEN_CONTAINER)
    kq = chay(
        "docker", "run", "-d", "--name", TEN_CONTAINER,
        "-e", "POSTGRES_PASSWORD=x", "-e", "POSTGRES_DB=kt", IMAGE_PG,
    )
    if kq.returncode != 0:
        raise SystemExit(f"không khởi động được PostgreSQL:\n{kq.stderr.decode()}")

    for _ in range(60):
        if chay("docker", "exec", TEN_CONTAINER,
                "pg_isready", "-U", "postgres", "-d", "kt").returncode == 0:
            break
        time.sleep(1)
    else:
        raise SystemExit("PostgreSQL không sẵn sàng sau 60 giây")

    for ten in ANH_PHAI_NAP:
        duong_dan = THU_MUC_MIGRATION / f"{ten}.sql"
        kq = chay(
            "docker", "exec", "-i", TEN_CONTAINER,
            "psql", "-U", "postgres", "-d", "kt", "-v", "ON_ERROR_STOP=1", "-q",
            dau_vao=duong_dan.read_bytes(),
        )
        if kq.returncode != 0:
            raise SystemExit(f"{ten} nạp không được:\n{kq.stderr.decode()[:2000]}")
        print(f"  nạp {ten}")


def don_db() -> None:
    chay("docker", "rm", "-f", TEN_CONTAINER)


# ----------------------------------------------------------------- kiểm tra


def san_pham_thieu_anh() -> list[list[str]]:
    return hang("""
        SELECT p.id, c.name, b.name, p.name
        FROM products p
        JOIN categories c ON c.id = p.category_id
        JOIN brands b ON b.id = p.brand_id
        WHERE NOT EXISTS (SELECT 1 FROM resources r
                          WHERE r.owner_type='PRODUCT_THUMBNAIL' AND r.owner_id = p.id)
        ORDER BY p.id
    """)


def kiem_tra() -> list[str]:
    """Trả về danh sách vấn đề tìm được; rỗng nghĩa là sạch."""
    van_de: list[str] = []

    tong_sp = mot_o("SELECT count(*) FROM products")
    tong_anh = mot_o("SELECT count(*) FROM resources WHERE owner_type='PRODUCT_THUMBNAIL'")
    print(f"\n  {tong_sp} sản phẩm, {tong_anh} ảnh sản phẩm")

    thieu = san_pham_thieu_anh()
    if thieu:
        van_de.append(f"{len(thieu)} sản phẩm không có ảnh nào")
        print(f"\n  ✗ {len(thieu)} sản phẩm KHÔNG có ảnh:")
        for sp_id, danh_muc, thuong_hieu, ten in thieu:
            print(f"      #{sp_id:>3}  [{danh_muc} / {thuong_hieu}]  {ten[:52]}")
    else:
        print("  ✓ mọi sản phẩm đều có ảnh")

    # Ảnh đại diện là ảnh sort_order = 0; thiếu nó thì thẻ sản phẩm không biết
    # lấy ảnh nào làm đại diện.
    thieu_dai_dien = hang("""
        SELECT p.id, p.name FROM products p
        WHERE EXISTS (SELECT 1 FROM resources r
                      WHERE r.owner_type='PRODUCT_THUMBNAIL' AND r.owner_id=p.id)
          AND NOT EXISTS (SELECT 1 FROM resources r
                          WHERE r.owner_type='PRODUCT_THUMBNAIL' AND r.owner_id=p.id
                            AND r.sort_order=0)
        ORDER BY p.id
    """)
    if thieu_dai_dien:
        van_de.append(f"{len(thieu_dai_dien)} sản phẩm có ảnh nhưng thiếu ảnh đại diện")
        print(f"  ✗ {len(thieu_dai_dien)} sản phẩm có ảnh nhưng không ảnh nào sort_order=0")
    else:
        print("  ✓ mọi sản phẩm có ảnh đều có ảnh đại diện")

    thieu_dm = hang("""
        SELECT c.id, c.name FROM categories c
        WHERE NOT EXISTS (SELECT 1 FROM resources r
                          WHERE r.owner_type='CATEGORY_THUMBNAIL' AND r.owner_id=c.id)
    """)
    if thieu_dm:
        van_de.append(f"{len(thieu_dm)} danh mục không có ảnh")
        print(f"  ✗ {len(thieu_dm)} danh mục không có ảnh")
    else:
        print("  ✓ mọi danh mục đều có ảnh")

    # Ảnh trỏ tới sản phẩm không tồn tại. Không có khoá ngoại nào bắt được vì
    # owner_id là đa hình - nó trỏ tới products hay categories tuỳ owner_type.
    mo_coi = hang("""
        SELECT r.id, r.owner_type::text, r.owner_id FROM resources r
        WHERE (r.owner_type='PRODUCT_THUMBNAIL'
               AND NOT EXISTS (SELECT 1 FROM products p WHERE p.id=r.owner_id))
           OR (r.owner_type='CATEGORY_THUMBNAIL'
               AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.id=r.owner_id))
        ORDER BY r.id
    """)
    if mo_coi:
        van_de.append(f"{len(mo_coi)} ảnh trỏ tới chủ sở hữu không tồn tại")
        print(f"  ✗ {len(mo_coi)} ảnh mồ côi:")
        for r_id, loai, chu in mo_coi[:10]:
            print(f"      resource #{r_id} -> {loai} #{chu} (không tồn tại)")
    else:
        print("  ✓ không có ảnh mồ côi")

    # Sequence lệch so với id lớn nhất: bản ghi đầu tiên do ứng dụng tạo sẽ đâm
    # vào id đã có và ném lỗi khoá chính. Seed chèn id tường minh nên phải tự gọi
    # setval, và việc đó rất dễ quên khi thêm dòng mới.
    print()
    for bang in ("products", "product_variants", "product_specifications",
                 "resources", "categories", "brands", "colors", "sizes"):
        lech = mot_o(f"""
            SELECT CASE WHEN last_value >= COALESCE((SELECT max(id) FROM {bang}), 0)
                        THEN 'ok' ELSE last_value || ' < ' || (SELECT max(id) FROM {bang})
                   END FROM {bang}_id_seq
        """)
        if lech != "ok":
            van_de.append(f"sequence {bang}_id_seq lệch ({lech})")
            print(f"  ✗ {bang}_id_seq đang ở {lech} — bản ghi mới sẽ đâm id trùng")
    if not any(v.startswith("sequence") for v in van_de):
        print("  ✓ mọi sequence đều vượt qua id lớn nhất")

    return van_de


def kiem_tra_url() -> list[str]:
    """Gọi HEAD từng URL ảnh. Một dòng ảnh tồn tại không có nghĩa ảnh hiện được."""
    urls = [d[0] for d in hang("SELECT DISTINCT url FROM resources ORDER BY url")]
    print(f"\n  kiểm tra {len(urls)} URL ảnh...")

    def thu(u: str) -> tuple[str, int]:
        yc = urllib.request.Request(u, method="HEAD")
        try:
            with urllib.request.urlopen(yc, timeout=20) as p:
                return u, p.status
        except urllib.error.HTTPError as e:
            return u, e.code
        except Exception:
            return u, 0

    with ThreadPoolExecutor(max_workers=16) as pool:
        ket_qua = list(pool.map(thu, urls))

    hong = [(u, m) for u, m in ket_qua if m != 200]
    if hong:
        print(f"  ✗ {len(hong)}/{len(urls)} URL không trả về 200:")
        for u, m in hong[:20]:
            print(f"      {m or 'không kết nối được'}  {u}")
        return [f"{len(hong)} URL ảnh hỏng"]
    print(f"  ✓ cả {len(urls)} URL đều trả về 200")
    return []


# --------------------------------------------------------------------- vá


def chon_nguoi_cho_muon() -> list[tuple[str, str, str, str, str]]:
    """Với mỗi sản phẩm thiếu ảnh, chọn một sản phẩm cho mượn ảnh đại diện.

    Luật cố định để chạy lại cho ra cùng kết quả: cùng danh mục VÀ cùng thương
    hiệu, ưu tiên sản phẩm có nhiều ảnh nhất, hoà thì lấy id nhỏ nhất. Cùng danh
    mục và cùng hãng nghĩa là ảnh mượn vẫn là đúng loại hàng của đúng hãng đó.
    """
    return [tuple(d) for d in hang("""
        WITH thieu AS (
            SELECT p.id, p.slug, p.name, p.category_id, p.brand_id
            FROM products p
            WHERE NOT EXISTS (SELECT 1 FROM resources r
                              WHERE r.owner_type='PRODUCT_THUMBNAIL' AND r.owner_id=p.id)
        ),
        ung_vien AS (
            SELECT p.id, p.category_id, p.brand_id, r.url,
                   (SELECT count(*) FROM resources x
                    WHERE x.owner_type='PRODUCT_THUMBNAIL' AND x.owner_id=p.id) AS so_anh
            FROM products p
            JOIN resources r ON r.owner_type='PRODUCT_THUMBNAIL'
                            AND r.owner_id=p.id AND r.sort_order=0
        )
        SELECT t.id, t.slug, t.name,
               (SELECT u.url FROM ung_vien u
                WHERE u.category_id=t.category_id AND u.brand_id=t.brand_id
                ORDER BY u.so_anh DESC, u.id ASC LIMIT 1),
               (SELECT u.id::text FROM ung_vien u
                WHERE u.category_id=t.category_id AND u.brand_id=t.brand_id
                ORDER BY u.so_anh DESC, u.id ASC LIMIT 1)
        FROM thieu t ORDER BY t.id
    """)]


def va_vao_v3() -> int:
    duong_dan = THU_MUC_MIGRATION / "V3__seed_initial_catalog.sql"
    noi_dung = duong_dan.read_text()

    # Gỡ khối cũ trước, để chạy --fix nhiều lần không chồng thêm dòng.
    noi_dung = re.sub(
        re.escape(DAU_KHOI_VA) + r".*?" + re.escape(CUOI_KHOI_VA) + r"\n?",
        "", noi_dung, flags=re.DOTALL,
    )

    can_va = chon_nguoi_cho_muon()
    if not can_va:
        duong_dan.write_text(noi_dung)
        return 0

    thieu_nguoi_cho = [d for d in can_va if not d[3]]
    if thieu_nguoi_cho:
        raise SystemExit(
            "không tìm được sản phẩm cùng danh mục và cùng hãng để mượn ảnh cho: "
            + ", ".join(f"#{d[0]}" for d in thieu_nguoi_cho)
        )

    id_ke_tiep = int(mot_o("SELECT max(id) FROM resources")) + 1
    dong = [
        DAU_KHOI_VA,
        "-- Những sản phẩm này về từ nguồn mà không kèm ảnh. Trang danh sách để lại",
        "-- một ô trống, trông như tải hỏng chứ không như sản phẩm chưa có ảnh.",
        "--",
        "-- Ảnh mượn từ một sản phẩm CÙNG danh mục và CÙNG thương hiệu, nên vẫn là",
        "-- đúng loại hàng của đúng hãng. public_id đặt theo slug của chính sản phẩm",
        "-- này chứ không lặp lại public_id của sản phẩm cho mượn: cột đó UNIQUE, và",
        "-- quan trọng hơn là nếu admin xoá ảnh ở đây thì Cloudinary chỉ báo",
        "-- \"not found\" (CloudinaryServiceImpl.deleteFile nuốt lỗi) chứ không xoá mất",
        "-- ảnh thật của sản phẩm cho mượn.",
        "--",
        "-- Sinh bởi tools/kiem_tra_seed.py --fix. Đừng sửa tay: chạy lại tool.",
    ]
    for sp_id, slug, ten, url, nguoi_cho in can_va:
        public_id = f"goodminton/products/{slug}-thumbnail"
        dong.append(f"-- #{sp_id} {ten[:60]}  (mượn của #{nguoi_cho})")
        dong.append(
            "INSERT INTO resources (id, public_id, url, type, owner_type, owner_id, "
            f"sort_order, created_at) VALUES ({id_ke_tiep}, '{public_id}', '{url}', "
            f"'IMAGE', 'PRODUCT_THUMBNAIL', {sp_id}, 0, NOW());"
        )
        id_ke_tiep += 1

    dong.append(f"SELECT setval('resources_id_seq', {id_ke_tiep - 1});")
    dong.append(CUOI_KHOI_VA)

    duong_dan.write_text(noi_dung.rstrip("\n") + "\n" + "\n".join(dong) + "\n")
    return len(can_va)


# ------------------------------------------------------------------- main


def main() -> int:
    bo_phan_tich = argparse.ArgumentParser(description=__doc__)
    bo_phan_tich.add_argument("--fix", action="store_true",
                              help="vá ảnh cho sản phẩm còn thiếu rồi kiểm tra lại")
    bo_phan_tich.add_argument("--check-urls", action="store_true",
                              help="gọi HEAD từng URL ảnh (chậm, cần mạng)")
    bo_phan_tich.add_argument("--keep", action="store_true",
                              help="giữ lại container để tự truy vấn thêm")
    tham_so = bo_phan_tich.parse_args()

    try:
        print("Dựng PostgreSQL dùng một lần và nạp seed:")
        dung_db()
        van_de = kiem_tra()
        if tham_so.check_urls:
            van_de += kiem_tra_url()

        if tham_so.fix and van_de:
            print("\nVá:")
            so = va_vao_v3()
            print(f"  đã thêm ảnh cho {so} sản phẩm vào V3__seed_initial_catalog.sql")
            print("\nNạp lại từ đầu để kiểm chứng:")
            dung_db()
            van_de = kiem_tra()
            if tham_so.check_urls:
                van_de += kiem_tra_url()

        print()
        if van_de:
            print("KẾT LUẬN: còn " + str(len(van_de)) + " vấn đề")
            for v in van_de:
                print(f"  - {v}")
            return 1
        print("KẾT LUẬN: seed sạch")
        return 0
    finally:
        if not tham_so.keep:
            don_db()


if __name__ == "__main__":
    sys.exit(main())
