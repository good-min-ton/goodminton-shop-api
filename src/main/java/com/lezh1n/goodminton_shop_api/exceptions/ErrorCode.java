package com.lezh1n.goodminton_shop_api.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Authentications & Authorizations(1001 - 1100)
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

    // Jwt & Security(1101 - 1200)
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
    PRODUCT_THUMBNAIL_BLANK(1505, "Vui lòng thêm ảnh sản phẩm", HttpStatus.BAD_REQUEST),
    PRODUCT_VARIANTS_REQUIRED(1506, "Vui lòng tạo variant cho sản phẩm", HttpStatus.BAD_REQUEST),

    // Brand (1601 - 1700)
    BRAND_NAME_BLANK(1601, "Tên thương hiệu không được để trống", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND(1602, "Không tìm thấy thương hiệu", HttpStatus.NOT_FOUND),
    BRAND_PRODUCT_EXISTED(1603, "Vui lòng xoá hết sản phẩm thuộc thương hiệu này", HttpStatus.BAD_REQUEST),

    // Version (1701 - 1800)
    VERSION_NAME_BLANK(1701, "Tên phiên bản không được để trống", HttpStatus.BAD_REQUEST),
    VERSION_NOT_FOUND(1702, "Không tìm thấy phiên bản", HttpStatus.BAD_REQUEST),
    VERSION_VARIANT_EXISTED(1703, "Vui lòng xoá hết các product variant chứa phiên bản hiện tại",
            HttpStatus.BAD_REQUEST),

    // Size (1801 - 1900)
    SIZE_NAME_BLANK(1801, "Tên size không được để trống", HttpStatus.BAD_REQUEST),
    SIZE_TYPE_BLANK(1802, "Loại size không được để trống", HttpStatus.BAD_REQUEST),
    SIZE_NOT_FOUND(1803, "Size không tồn tại", HttpStatus.NOT_FOUND),
    SIZE_VARIANT_EXISTED(1804, "Vui lòng xoá hết các product variant chứa size hiện tại", HttpStatus.BAD_REQUEST),

    // Color (1901 - 2000)
    COLOR_NAME_BLANK(1901, "Tên màu không được để trống", HttpStatus.BAD_REQUEST),
    COLOR_NOT_FOUND(1902, "Không tìm thấy màu", HttpStatus.BAD_REQUEST),
    COLOR_VARIANT_EXISTED(1903, "Vui lòng xoá hết các product variant chứa màu hiện tại", HttpStatus.BAD_REQUEST),

    // Product variant(2001 - 2100)
    VARIANT_NOT_FOUND(2001, "Biến thể sản phẩm này không tồn tại", HttpStatus.NOT_FOUND),
    VARIANT_PRODUCT_BLANK(2002, "Sản phẩm không được để trống", HttpStatus.BAD_REQUEST),
    VARIANT_VERSION_BLANK(2003, "Bản thể không được để trống", HttpStatus.BAD_REQUEST),
    VARIANT_COLOR_BLANK(2004, "Màu sắc không được để trống", HttpStatus.BAD_REQUEST),
    VARIANT_SIZE_BLANK(2005, "Size khong được để trống", HttpStatus.BAD_REQUEST),
    VARIANT_PRICE_BLANK(2006, "Giá không được để trống", HttpStatus.BAD_REQUEST),
    VARIANT_PRICE_MUST_BE_POSITIVE(2007, "Giá tiền phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    VARIANT_QUANTITY_BLANK(2008, "Số lượng hàng không được để trống", HttpStatus.BAD_REQUEST),
    VARIANT_LIST_SIZES_BLANK(2009, "Danh sách các size không được đê trống", HttpStatus.BAD_REQUEST),
    VARIANT_NOT_BELONG_TO_PRODUCT(2010, "Bản thể truyền vào không thuộc danh sách bản thể của product",
            HttpStatus.BAD_REQUEST),
    VARIANT_DUPLICATE_COMBINATION(2011, "Tổ hợp phiên bản và màu sắc đã tồn tại cho sản phảm này",
            HttpStatus.CONFLICT),

    // Product specification (2101 - 2200)
    SPEC_NOT_FOUND(2101, "Không tìm thấy thông số này", HttpStatus.NOT_FOUND),
    SPEC_NAME_REQUIRED(2102, "Tên thông số không được để trống", HttpStatus.BAD_REQUEST),
    SPEC_VALUE_REQUIRED(2103, "Giá trị của thông số không được đê trống", HttpStatus.BAD_REQUEST),
    SPEC_NOT_BELONG_TO_PRODUCT(2104, "Thông số truyèn vào không danh sách thông số của sản phẩm",
            HttpStatus.BAD_REQUEST),
    SPEC_DUPLICATE(2105, "Thông số đã tồn tại cho sản phẩm", HttpStatus.CONFLICT),

    // Variant image (2201 - 2300)
    VARIANT_IMAGE_URL_REQUIRED(2201, "Link ảnh không được đê trống", HttpStatus.BAD_REQUEST),
    VARIANT_IMAGE_PUBLIC_ID_DUPLICATE(2202, "Ảnh này đã đựợc truyền cho biến thể khác", HttpStatus.CONFLICT),

    // Variant size (2301- 2400)
    VARIANT_SIZE_NOT_FOUND(2301, "Không tìm thấy tổ hợp variant với size này", HttpStatus.NOT_FOUND),
    VARIANT_SIZE_ID_BLANK(2302, "VariantSize ID không được để trống", HttpStatus.BAD_REQUEST),

    // Inventory (2401 - 2500)
    INVENTORY_NOT_FOUND(2401, "Không tim thấy kho hàng này", HttpStatus.NOT_FOUND),
    INVENTORY_VARIANT_NOT_FOUND(2402,
            "Bản thể của sản phẩm hiện tại không thuộc bất kỳ kho của cửa hàng nào. Vui lòng chọn thêm sản phẩm vào kho cửa hàng trước",
            HttpStatus.BAD_REQUEST),
    INVENTORY_STORE_ID_REQUIRED(2403, "Store ID không đực để trống", HttpStatus.BAD_REQUEST),
    INVENTORY_VARIANT_SIZE_REQUIRED(2404, "VariantSize ID không được để trống", HttpStatus.BAD_REQUEST),
    INVENTORY_QUANTITY_REQUIRED(2405, "Số lượng sản phẩm không được để trống", HttpStatus.BAD_REQUEST),
    INVENTORY_STORE_AND_VARIANT_DUPLICATED(2406, "Kho của cửa hàng đã tồn tại bản thể của sản phẩm này",
            HttpStatus.BAD_REQUEST),

    // Order (2501 - 2600)
    ORDER_NOT_FOUND(2501, "Không tìm thây đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_INVENTORY_INSUFFICIENT(2502, "Số lượng hàng trong kho không đủ", HttpStatus.BAD_REQUEST),
    ORDER_INVALID_STATUS(2503, "Trạng thái đơn hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_CANCEL_COMPLETED(2504, "Không thể huỷ đơn hàng đã hoàn thành", HttpStatus.BAD_REQUEST),
    ORDER_CANCEL_ALREADY_CANCELLED(2505, "Không thể huỷ đơn hàng đã được huỷ trước đó", HttpStatus.BAD_REQUEST),

    // Order item (2601 - 2700)
    ORDER_ITEM_NOT_FOUND(2601, "Không tìm thấy chi tiết đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_ITEM_LIST_EMPTY(2602, "Danh sách đơn hàng không được để trống", HttpStatus.BAD_REQUEST),
    ORDER_PAYMENT_METHOD_NULL(2603, "Phương thức thanh toán không được để trống", HttpStatus.BAD_REQUEST),

    // Allocate order (2701 - 2800)
    ALLOCATION_QUANTITY_INVALID(2701, "Số lượng sản phầm phân phối không khớp với đơn hàng", HttpStatus.BAD_REQUEST),
    ALLOCATION_VARIANT_SIZE_INVALID(2702, "Bản phối sản phẩm không khớp vơi đơn hàng", HttpStatus.BAD_REQUEST),
    ALLOCATION_LIST_EMPTY(2703, "Danh sách sản phẩm cần phân phối không được để trống", HttpStatus.BAD_REQUEST),

    // Payment (2801 - 2900)
    PAYMENT_NOT_FOUND(2801, "Không tìm thấy giao dích", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PAID(2802, "Giao dịch đã được thanh toán", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_STATUS(2803, "Trạng thái giao dịch không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_CREATION_FAILED(2804, "Tạo giao dịch thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_WEBHOOK_FAILED(2805, "Webhook processing failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // Product discount(2901 - 3000)
    DISCOUNT_SALE_PRICE_NULL(2901, "Giá sale không được đê trống", HttpStatus.BAD_REQUEST),
    DISCOUNT_START_TIME_NULL(2902, "Thời gian bắt đầu sale không được để trống", HttpStatus.BAD_REQUEST),
    DISCOUNT_END_TIME_NULL(2903, "Thời gian kết thúc sale không được đê trống", HttpStatus.BAD_REQUEST),
    DISCOUNT_START_TIME_BEFORE_NOW(2904, "Thời gian bắt đầu phải từ sau thòi điểm hiện tại", HttpStatus.BAD_REQUEST),
    DISCOUNT_END_TIME_BEFORE_START_TIME(2905, "Thời gian kết thúc phải sau thời gian bắt đầu", HttpStatus.BAD_REQUEST),
    DISCOUNT_EXISTED(2906, "Đã tồn tại discount của variant size trong khoảng thời gian này", HttpStatus.BAD_REQUEST),

    // Review (3001 - 3100)
    REVIEW_NOT_FOUND(3001, "Không tồn tại review này", HttpStatus.NOT_FOUND),
    REVIEW_RATING_OUT_OF_RANGE(3002, "Rating không thuộc khoảng hợp lệ (1 - 5)", HttpStatus.BAD_REQUEST),
    REVIEW_RATING_BLANK(3003, "Rating không được để trống", HttpStatus.BAD_REQUEST),

    // Database constraint violations(9601 - 9700)
    DATABASE_CONSTRAINT_VIOLATION(9601, "Vi phạm ràng buộc dữ liệu", HttpStatus.BAD_REQUEST),
    DATABASE_DUPLICATE_KEY(9602, "Dữ liệu đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    DATABASE_FOREIGN_KEY_VIOLATION(9603, "Vi phạm ràng buộc khóa ngoại", HttpStatus.BAD_REQUEST),
    DATABASE_UNIQUE_CONSTRAINT_VIOLATION(9604, "Dữ liệu bị trùng lặp", HttpStatus.CONFLICT),

    // File errors
    FILE_EMPTY(9701, "File ảnh không được để trống", HttpStatus.BAD_REQUEST),
    FILE_TYPE_NOT_SUPPORTED(9702, "Loại file không được hỗ trợ", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(9703, "Có lỗi xảy ra khi upload file ảnh", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(9704, "File size exceeds maximum allowed limit", HttpStatus.BAD_REQUEST),

    // Data errors
    ENUM_INVALID_VALUE(9800, "Invalid enum value", HttpStatus.BAD_REQUEST),

    // System errors (9901 - 9999)
    SYSTEM_UNKNOWN_ERROR(9998, "System unknow error", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_INTERNAL_ERROR(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private int code;
    private String message;
    private HttpStatus status;
}
