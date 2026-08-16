-- uq_variant chỉ chặn được trùng khi CẢ HAI trục màu và cỡ đều có giá trị.
-- PostgreSQL coi NULL là khác nhau trong ràng buộc UNIQUE, nên hai variant cùng
-- một sản phẩm mà đều "không phân màu, không phân cỡ" thì lọt qua hoàn toàn.
--
-- Đó không phải chuyện lý thuyết: với dây cước và phụ kiện, admin chọn
-- "— Không phân màu —" và "— Không phân cỡ —" là mặc định, nên hai variant y hệt
-- nhau rất dễ xuất hiện. Khi đó storefront lấy variant nào là chuyện may rủi.
--
-- NULLS NOT DISTINCT (PostgreSQL 15+) làm NULL va chạm với NULL. Máy chủ chạy
-- pgvector/pgvector:pg15 nên dùng được.

-- Dừng sớm với thông báo đọc được, thay vì để Postgres ném lỗi tạo index thô.
-- Ràng buộc mới không thể áp lên dữ liệu đã trùng, và khi Flyway hỏng thì cả
-- lần deploy dừng lại - người trực cần biết ngay phải sửa gì.
DO $$
DECLARE
    so_trung int;
BEGIN
    SELECT count(*) INTO so_trung FROM (
        SELECT product_id, color_id, size_id
        FROM product_variants
        GROUP BY product_id, color_id, size_id
        HAVING count(*) > 1
    ) t;

    IF so_trung > 0 THEN
        RAISE EXCEPTION
            'Còn % tổ hợp (product_id, color_id, size_id) bị trùng. Hợp nhất hoặc xoá bớt trước khi chạy migration này. Xem danh sách: SELECT product_id, color_id, size_id, count(*), array_agg(id) FROM product_variants GROUP BY 1,2,3 HAVING count(*) > 1;',
            so_trung;
    END IF;
END $$;

ALTER TABLE product_variants DROP CONSTRAINT uq_variant;

ALTER TABLE product_variants
    ADD CONSTRAINT uq_variant UNIQUE NULLS NOT DISTINCT (product_id, color_id, size_id);
