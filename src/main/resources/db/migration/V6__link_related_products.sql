-- ============================================================
-- V6__link_related_products.sql
-- Populate products.related_product_id for sibling product
-- families detected from name patterns (color/variant siblings).
-- The column already exists in V1 — this migration only updates
-- data, no DDL needed.
-- ============================================================

-- 48 family roots cover 67 child products.

-- family root #2: Vợt cầu lông Yonex Astrox 77 Play Limited - Light Beige chính hãng
UPDATE products SET related_product_id = 2 WHERE id IN (7);
-- family root #3: Vợt cầu lông Yonex Astrox 100 Tour VA
UPDATE products SET related_product_id = 3 WHERE id IN (9);
-- family root #4: Vợt cầu lông Yonex Astrox 99 Play 2025
UPDATE products SET related_product_id = 4 WHERE id IN (10, 11, 12);
-- family root #21: Vợt cầu lông Lining Axforce Cannon Pro Limited 2026 chính hãng
UPDATE products SET related_product_id = 21 WHERE id IN (38);
-- family root #24: Vợt Cầu Lông Lining Axforce 90 Limited 2026 chính hãng
UPDATE products SET related_product_id = 24 WHERE id IN (29);
-- family root #33: Set Vợt Cầu Lông Lining Bladex 900 Master New 2024
UPDATE products SET related_product_id = 33 WHERE id IN (39);
-- family root #65: Giày cầu lông Yonex Subaxia GT Women - Light Gray chính hãng
UPDATE products SET related_product_id = 65 WHERE id IN (66, 67);
-- family root #71: Giày cầu lông Yonex SHB 65Z VA Women - Grayish Beige chính hãng
UPDATE products SET related_product_id = 71 WHERE id IN (72);
-- family root #73: Giày cầu lông Yonex 88 Dial 3 Wide 2025
UPDATE products SET related_product_id = 73 WHERE id IN (74);
-- family root #81: Giày cầu lông Lining AYAV001-5 Saga 3 Pro Chính Hãng
UPDATE products SET related_product_id = 81 WHERE id IN (84, 85, 87);
-- family root #82: Giày cầu lông Lining AYTV021-5 chính hãng
UPDATE products SET related_product_id = 82 WHERE id IN (83);
-- family root #86: Giày cầu lông Lining AYTV013-1 chính hãng
UPDATE products SET related_product_id = 86 WHERE id IN (88);
-- family root #90: Giày cầu lông Lining AYTT001-6 chính hãng
UPDATE products SET related_product_id = 90 WHERE id IN (95);
-- family root #91: Giày cầu lông Lining AYZV001-2 chính hãng
UPDATE products SET related_product_id = 91 WHERE id IN (92);
-- family root #94: Giày cầu lông Lining AYTU001-9 chính hãng
UPDATE products SET related_product_id = 94 WHERE id IN (96, 100);
-- family root #101: Giày cầu lông Victor AS-17 W/CR chính hãng
UPDATE products SET related_product_id = 101 WHERE id IN (102);
-- family root #103: Giày cầu lông Victor AS-10 W/CX chính hãng
UPDATE products SET related_product_id = 103 WHERE id IN (104);
-- family root #121: Áo cầu lông Yonex TPM2899 - Oatmeal chính hãng
UPDATE products SET related_product_id = 121 WHERE id IN (122);
-- family root #125: Áo cầu lông Yonex TPM3243 - White chính hãng
UPDATE products SET related_product_id = 125 WHERE id IN (126);
-- family root #127: Áo cầu lông Yonex TRM3240 - Trooper chính hãng
UPDATE products SET related_product_id = 127 WHERE id IN (128, 129, 130);
-- family root #131: Áo cầu lông Yonex TRM3222 - Cornflower Blue chính hãng
UPDATE products SET related_product_id = 131 WHERE id IN (132);
-- family root #138: Áo cầu lông Yonex TRM2994 -  Georgia Peach chính hãng
UPDATE products SET related_product_id = 138 WHERE id IN (139, 140);
-- family root #141: Áo cầu lông Lining P-ATSU493-4 nam chính hãng
UPDATE products SET related_product_id = 141 WHERE id IN (144);
-- family root #142: Áo cầu lông Lining P-APLR125-7 nam chính hãng
UPDATE products SET related_product_id = 142 WHERE id IN (143);
-- family root #146: Áo cầu lông Lining P-ATSUB07-1 nam chính hãng
UPDATE products SET related_product_id = 146 WHERE id IN (149);
-- family root #156: Áo cầu lông Lining 3175 nữ - Trắng
UPDATE products SET related_product_id = 156 WHERE id IN (157);
-- family root #158: Áo cầu lông Lining 3173 nữ - Trắng hồng
UPDATE products SET related_product_id = 158 WHERE id IN (160);
-- family root #162: Áo cầu lông Victor 2117 Nữ - Tím
UPDATE products SET related_product_id = 162 WHERE id IN (168);
-- family root #164: Áo hoodie lót bông Victor Vic02 - Xanh Than
UPDATE products SET related_product_id = 164 WHERE id IN (177);
-- family root #166: Áo cầu lông Victor 846 Nam - Trắng đen
UPDATE products SET related_product_id = 166 WHERE id IN (167, 169);
-- family root #170: Áo cầu lông Victor 2118 Nữ - Trắng đen
UPDATE products SET related_product_id = 170 WHERE id IN (171, 172, 173, 174);
-- family root #179: Áo hoodie lót bông Victor Vic01 - Xanh dương
UPDATE products SET related_product_id = 179 WHERE id IN (180);
-- family root #183: Quần Cầu Lông Yonex QY2301 Nữ - Đen Logo Đỏ
UPDATE products SET related_product_id = 183 WHERE id IN (184);
-- family root #185: Quần cầu lông Yonex TSM3250 - Blue Depths chính hãng
UPDATE products SET related_product_id = 185 WHERE id IN (186);
-- family root #187: Quần cầu lông Yonex TSM3249 - Trooper chính hãng
UPDATE products SET related_product_id = 187 WHERE id IN (188, 189);
-- family root #190: Quần cầu lông Yonex TSM3248 - Blue Shadow chính hãng
UPDATE products SET related_product_id = 190 WHERE id IN (191, 192);
-- family root #194: Quần cầu lông Yonex TSM3064 - Poppy Seed chính hãng
UPDATE products SET related_product_id = 194 WHERE id IN (195);
-- family root #196: Quần cầu lông Yonex TSM3085 - Jet Black chính hãng
UPDATE products SET related_product_id = 196 WHERE id IN (197, 198);
-- family root #201: Quần cầu lông Lining 967 - Xanh navy
UPDATE products SET related_product_id = 201 WHERE id IN (202, 204);
-- family root #217: Quần cầu lông Lining 9682 - Đen đỏ
UPDATE products SET related_product_id = 217 WHERE id IN (218, 219);
-- family root #221: Quần cầu lông Victor 901 - Trắng kem
UPDATE products SET related_product_id = 221 WHERE id IN (236);
-- family root #222: Quần cầu lông Victor 621 - Trắng
UPDATE products SET related_product_id = 222 WHERE id IN (226);
-- family root #223: Quần cầu lông Victor 960 - Trắng
UPDATE products SET related_product_id = 223 WHERE id IN (224);
-- family root #231: Quần cầu lông Victor nữ trắng - mã 435
UPDATE products SET related_product_id = 231 WHERE id IN (232);
-- family root #233: Quần cầu lông Victor nữ đen - Mã 380
UPDATE products SET related_product_id = 233 WHERE id IN (234);
-- family root #247: Dây cước căng vợt Lining No.1 cuộn
UPDATE products SET related_product_id = 247 WHERE id IN (258);
-- family root #251: Dây cước căng vợt cầu lông Lining N70
UPDATE products SET related_product_id = 251 WHERE id IN (255);
-- family root #260: Dây cước căng vợt cầu lông Victor VBS-70 Power cuộn
UPDATE products SET related_product_id = 260 WHERE id IN (261, 262);
