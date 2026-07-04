package com.lezh1n.goodminton_shop_api.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Auth & Authorization (1001 - 1100)
    AUTH_EMAIL_EXISTED(1001, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    AUTH_PHONE_EXISTED(1002, "Số điện thoại đã được sử dụng", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_CREDENTIALS(1003, "Thông tin đăng nhập không hợp lệ", HttpStatus.BAD_REQUEST),
    AUTH_UNAUTHORIZED(1004, "Unauthorized", HttpStatus.UNAUTHORIZED),
    AUTH_UNAUTHENTICATED(1005, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_PHONE(1006, "Số điện thoại phải có ít nhất 10 số và nhiều nhất 12 số", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_PASSWORD(1007, "Mật khẩu phải có ít nhất 8 ký tự", HttpStatus.BAD_REQUEST),
    AUTH_FULLNAME_BLANK(1008, "Họ tên không được để trống", HttpStatus.BAD_REQUEST),
    AUTH_EMAIL_BLANK(1009, "Email không được để trống", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_EMAIL(1010, "Email không đúng định dạng", HttpStatus.BAD_REQUEST),
    AUTH_ACCOUNT_INACTIVE(1011, "Tài khoản của bạn đã bị khoá", HttpStatus.FORBIDDEN),

    // JWT & Security (1101 - 1200)
    JWT_GENERATION_ERROR(1101, "Jwt generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_INVALID_TOKEN(1102, "Invalid token", HttpStatus.BAD_REQUEST),
    JWT_EXPIRED_TOKEN(1103, "Token is expired", HttpStatus.BAD_REQUEST),
    JWT_TOKEN_BLACKLISTED(1104, "Token has been blacklisted", HttpStatus.BAD_REQUEST),

    // Account (1201 - 1300)
    ACCOUNT_NOT_FOUND(1201, "User not found", HttpStatus.NOT_FOUND),
    ACCOUNT_OLD_PASSWORD_NOT_MATCH(1202, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST),
    ACCOUNT_NEW_PASSWORD_SAME_AS_OLD(1203, "Mật khẩu mới không được trùng mật khẩu cũ", HttpStatus.BAD_REQUEST),
    ACCOUNT_EMAIL_RESET_SEND_FAILED(1204, "Reset password email fail to send", HttpStatus.BAD_REQUEST),
    ACCOUNT_RESET_TOKEN_BLANK(1205, "Reset token could not be blank", HttpStatus.BAD_REQUEST),
    ACCOUNT_RESET_TOKEN_INVALID(1206, "Link reset mật khẩu không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    ACCOUNT_PASSWORD_SAME_AS_OLD(1207, "Mật khẩu mới không được trùng với mật khẩu cũ", HttpStatus.BAD_REQUEST),
    ACCOUNT_CANT_BE_LOCKED(1208, "Không thể khóa tải khoản super admin", HttpStatus.BAD_REQUEST),

    // Store (1301 - 1400)
    STORE_NOT_FOUND(1301, "Store not found", HttpStatus.NOT_FOUND),
    STORE_ASSIGN_NON_ADMIN_ACCOUNT(1302, "You can only assign store admin account to a store",
            HttpStatus.BAD_REQUEST),
    STORE_ADMIN_ASSIGNED(1303, "Admin has been assigned with another store", HttpStatus.BAD_REQUEST),
    STORE_ADMIN_ID_REQUIRED(1304, "Store admin id required", HttpStatus.BAD_REQUEST),
    STORE_NAME_BLANK(1305, "Tên chi nhánh không được để trống", HttpStatus.BAD_REQUEST),
    STORE_ADDRESS_BLANK(1306, "Địa chỉ của chi nhánh không được để trống", HttpStatus.BAD_REQUEST),
    STORE_LOCATION_INFO_BLANK(1307, "Thông tin vị trí của chi nhánh không được để trống", HttpStatus.BAD_REQUEST),
    STORE_ADMIN_BLANK(1308, "Admin id của chi nhánh không được để trống", HttpStatus.BAD_REQUEST),
    STORE_INVENTORY_EXISTED(1309, "Cửa hàng vẫn tồn tại kho sản phẩm", HttpStatus.BAD_REQUEST),
    STORE_NO_CENTRAL(1310, "Không tìm thấy kho trung tâm — vui lòng đặt is_central = true cho 1 store",
            HttpStatus.INTERNAL_SERVER_ERROR),

    // Category (1401 - 1500)
    CATEGORY_NAME_REQUIRED(1401, "Tên danh mục không được để trống", HttpStatus.BAD_REQUEST),
    CATEGORY_DESCRIPTION_REQUIRED(1402, "Phần mô tả danh mục không được để trống", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1403, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_PRODUCT_EXISTED(1404, "Danh mục hiện đang chứa có sản phẩm", HttpStatus.BAD_REQUEST),

    // Product (1501 - 1600)
    PRODUCT_NOT_FOUND(1501, "Sản phẩm không tồn tại", HttpStatus.NOT_FOUND),
    PRODUCT_CATEGORY_BLANK(1502, "Danh mục không được để trống", HttpStatus.BAD_REQUEST),
    PRODUCT_BRAND_BLANK(1503, "Thương hiệu không được để trống", HttpStatus.BAD_REQUEST),
    PRODUCT_NAME_BLANK(1504, "Tên sản phẩm không được để trống", HttpStatus.BAD_REQUEST),
    PRODUCT_VARIANTS_REQUIRED(1505, "Vui lòng tạo variant cho sản phẩm", HttpStatus.BAD_REQUEST),
    PRODUCT_SLUG_BLANK(1506, "Slug sản phẩm không được để trống", HttpStatus.BAD_REQUEST),
    PRODUCT_SLUG_EXISTED(1507, "Slug đã được sử dụng", HttpStatus.CONFLICT),
    PRODUCT_RELATED_MUST_BE_ROOT(1508, "Chỉ được liên kết với sản phẩm gốc", HttpStatus.BAD_REQUEST),
    PRODUCT_HAS_RELATED_CHILDREN(1509, "Sản phẩm đang có các phiên bản liên kết", HttpStatus.CONFLICT),

    // Brand (1601 - 1700)
    BRAND_NAME_BLANK(1601, "Tên thương hiệu không được để trống", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND(1602, "Không tìm thấy thương hiệu", HttpStatus.NOT_FOUND),
    BRAND_PRODUCT_EXISTED(1603, "Vui lòng xoá hết sản phẩm thuộc thương hiệu này", HttpStatus.BAD_REQUEST),

    // Size (1701 - 1800)
    SIZE_NAME_BLANK(1701, "Tên size không được để trống", HttpStatus.BAD_REQUEST),
    SIZE_TYPE_BLANK(1702, "Loại size không được để trống", HttpStatus.BAD_REQUEST),
    SIZE_NOT_FOUND(1703, "Size không tồn tại", HttpStatus.NOT_FOUND),
    SIZE_VARIANT_EXISTED(1704, "Vui lòng xoá hết các product variant chứa size hiện tại", HttpStatus.BAD_REQUEST),

    // Color (1801 - 1900)
    COLOR_NAME_BLANK(1801, "Tên màu không được để trống", HttpStatus.BAD_REQUEST),
    COLOR_NOT_FOUND(1802, "Không tìm thấy màu", HttpStatus.BAD_REQUEST),
    COLOR_VARIANT_EXISTED(1803, "Vui lòng xoá hết các product variant chứa màu hiện tại", HttpStatus.BAD_REQUEST),

    // Product variant (1901 - 2000)
    VARIANT_NOT_FOUND(1901, "Biến thể sản phẩm này không tồn tại", HttpStatus.NOT_FOUND),
    VARIANT_COLOR_BLANK(1902, "Màu sắc không được để trống", HttpStatus.BAD_REQUEST),
    VARIANT_PRICE_BLANK(1903, "Giá không được để trống", HttpStatus.BAD_REQUEST),
    VARIANT_PRICE_MUST_BE_POSITIVE(1904, "Giá tiền phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    VARIANT_DUPLICATE_COMBINATION(1905, "Tổ hợp color/size đã tồn tại cho sản phẩm này", HttpStatus.CONFLICT),
    VARIANT_SKU_BLANK(1906, "SKU không được để trống", HttpStatus.BAD_REQUEST),
    VARIANT_SKU_EXISTED(1907, "SKU đã được sử dụng", HttpStatus.CONFLICT),
    VARIANT_IN_USE(1908,
            "Không thể xoá variant đang có tồn kho hoặc đơn hàng. Hãy xoá inventory liên quan trước.",
            HttpStatus.CONFLICT),

    // Product specification (2001 - 2100)
    SPEC_NAME_REQUIRED(2001, "Tên thông số không được để trống", HttpStatus.BAD_REQUEST),
    SPEC_VALUE_REQUIRED(2002, "Giá trị của thông số không được để trống", HttpStatus.BAD_REQUEST),

    // Variant image (2101 - 2200)
    VARIANT_IMAGE_NOT_FOUND(2101, "Không tìm thấy ảnh", HttpStatus.NOT_FOUND),
    VARIANT_IMAGE_PUBLIC_ID_DUPLICATE(2102, "Ảnh này đã được dùng cho biến thể khác", HttpStatus.CONFLICT),

    // Inventory (2201 - 2300)
    INVENTORY_NOT_FOUND(2201, "Không tìm thấy kho hàng này", HttpStatus.NOT_FOUND),
    INVENTORY_VARIANT_NOT_FOUND(2202, "Variant chưa có inventory ở store này", HttpStatus.BAD_REQUEST),
    INVENTORY_STORE_ID_REQUIRED(2203, "Store ID không được để trống", HttpStatus.BAD_REQUEST),
    INVENTORY_VARIANT_ID_REQUIRED(2204, "Variant ID không được để trống", HttpStatus.BAD_REQUEST),
    INVENTORY_QUANTITY_REQUIRED(2205, "Số lượng sản phẩm không được để trống", HttpStatus.BAD_REQUEST),
    INVENTORY_QUANTITY_NEGATIVE(2206, "Số lượng tồn kho không được âm", HttpStatus.BAD_REQUEST),
    INVENTORY_FORBIDDEN(2207, "Không có quyền cập nhật kho hàng này", HttpStatus.FORBIDDEN),

    // Order (2301 - 2400)
    ORDER_NOT_FOUND(2301, "Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_INVENTORY_INSUFFICIENT(2302, "Số lượng hàng trong kho không đủ", HttpStatus.BAD_REQUEST),
    ORDER_INVALID_STATUS(2303, "Trạng thái đơn hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_CANCEL(2304, "Không thể huỷ đơn hàng ở trạng thái hiện tại", HttpStatus.BAD_REQUEST),
    ORDER_FORBIDDEN(2305, "Không có quyền truy cập đơn hàng này", HttpStatus.FORBIDDEN),
    ORDER_ITEMS_REQUIRED(2306, "Đơn hàng phải có ít nhất 1 sản phẩm", HttpStatus.BAD_REQUEST),
    ORDER_RECIPIENT_NAME_REQUIRED(2307, "Tên người nhận không được để trống", HttpStatus.BAD_REQUEST),
    ORDER_RECIPIENT_PHONE_REQUIRED(2308, "SĐT người nhận không được để trống", HttpStatus.BAD_REQUEST),
    ORDER_RECIPIENT_ADDRESS_REQUIRED(2309, "Địa chỉ người nhận không được để trống", HttpStatus.BAD_REQUEST),
    ORDER_RECIPIENT_EMAIL_INVALID(2310, "Email người nhận không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_PAYMENT_METHOD_REQUIRED(2311, "Phương thức thanh toán không được để trống", HttpStatus.BAD_REQUEST),
    ORDER_SHIPPING_CODE_REQUIRED(2313, "Mã vận đơn không được để trống", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_NOT_FOUND(2314, "Không tìm thấy chi tiết đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_ITEM_VARIANT_REQUIRED(2315, "Variant ID không được để trống", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_QUANTITY_REQUIRED(2316, "Số lượng không được để trống", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_QUANTITY_INVALID(2317, "Số lượng phải lớn hơn 0", HttpStatus.BAD_REQUEST),

    // Payment (2401 - 2500)
    PAYMENT_NOT_FOUND(2401, "Không tìm thấy giao dịch", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PAID(2402, "Đơn hàng đã được thanh toán", HttpStatus.BAD_REQUEST),
    PAYMENT_ORDER_ID_REQUIRED(2403, "Order ID không được để trống", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_SIGNATURE(2404, "Chữ ký VNPay không hợp lệ", HttpStatus.BAD_REQUEST),

    // Review (2501 - 2600)
    REVIEW_RATING_OUT_OF_RANGE(2501, "Rating không thuộc khoảng hợp lệ (1 - 5)", HttpStatus.BAD_REQUEST),
    REVIEW_RATING_BLANK(2502, "Rating không được để trống", HttpStatus.BAD_REQUEST),
    REVIEW_ORDER_ITEM_BLANK(2503, "Order item id không được để trống", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(2504, "Bạn đã review chi tiết đơn hàng này", HttpStatus.CONFLICT),

    // Resource (2601 - 2700)
    RESOURCE_NOT_FOUND(2601, "Không tìm thấy resource", HttpStatus.NOT_FOUND),
    RESOURCE_REORDER_INVALID(2602, "Danh sách reorder không khớp với resource hiện có", HttpStatus.BAD_REQUEST),

    // Database constraint violations (9001 - 9100)
    DATABASE_CONSTRAINT_VIOLATION(9001, "Vi phạm ràng buộc dữ liệu", HttpStatus.BAD_REQUEST),
    DATABASE_DUPLICATE_KEY(9002, "Dữ liệu đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    DATABASE_FOREIGN_KEY_VIOLATION(9003, "Vi phạm ràng buộc khóa ngoại", HttpStatus.BAD_REQUEST),
    DATABASE_UNIQUE_CONSTRAINT_VIOLATION(9004, "Dữ liệu bị trùng lặp", HttpStatus.CONFLICT),

    // File errors (9101 - 9200)
    FILE_EMPTY(9101, "File không được để trống", HttpStatus.BAD_REQUEST),
    FILE_TYPE_NOT_SUPPORTED(9102, "Loại file không được hỗ trợ", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(9103, "Upload file thất bại", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(9104, "File size exceeds maximum allowed limit", HttpStatus.BAD_REQUEST),

    // Data errors (9201 - 9300)
    ENUM_INVALID_VALUE(9201, "Invalid enum value", HttpStatus.BAD_REQUEST),

    // System errors (9901 - 9999)
    SYSTEM_UNKNOWN_ERROR(9998, "System unknown error", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_INTERNAL_ERROR(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private int code;
    private String message;
    private HttpStatus status;
}
