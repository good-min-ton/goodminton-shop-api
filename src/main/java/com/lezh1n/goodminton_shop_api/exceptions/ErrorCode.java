package com.lezh1n.goodminton_shop_api.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Auth & Authorization (1001 - 1100)
    AUTH_EMAIL_EXISTED(1001, "Email is already in use", HttpStatus.BAD_REQUEST),
    AUTH_PHONE_EXISTED(1002, "Phone number is already in use", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_CREDENTIALS(1003, "Invalid credentials", HttpStatus.BAD_REQUEST),
    AUTH_UNAUTHORIZED(1004, "Unauthorized", HttpStatus.UNAUTHORIZED),
    AUTH_UNAUTHENTICATED(1005, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_PHONE(1006, "Phone number must be 10 to 12 digits", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_PASSWORD(1007, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    AUTH_FULLNAME_BLANK(1008, "Full name is required", HttpStatus.BAD_REQUEST),
    AUTH_EMAIL_BLANK(1009, "Email is required", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_EMAIL(1010, "Invalid email format", HttpStatus.BAD_REQUEST),
    AUTH_ACCOUNT_INACTIVE(1011, "Account is locked", HttpStatus.FORBIDDEN),

    // JWT & Security (1101 - 1200)
    JWT_GENERATION_ERROR(1101, "JWT generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_INVALID_TOKEN(1102, "Invalid token", HttpStatus.BAD_REQUEST),
    JWT_EXPIRED_TOKEN(1103, "Token has expired", HttpStatus.BAD_REQUEST),
    JWT_TOKEN_BLACKLISTED(1104, "Token has been blacklisted", HttpStatus.BAD_REQUEST),

    // Account (1201 - 1300)
    ACCOUNT_NOT_FOUND(1201, "User not found", HttpStatus.NOT_FOUND),
    ACCOUNT_OLD_PASSWORD_NOT_MATCH(1202, "Old password is incorrect", HttpStatus.BAD_REQUEST),
    ACCOUNT_NEW_PASSWORD_SAME_AS_OLD(1203, "New password must differ from the old password", HttpStatus.BAD_REQUEST),
    ACCOUNT_EMAIL_RESET_SEND_FAILED(1204, "Failed to send the reset-password email", HttpStatus.BAD_REQUEST),
    ACCOUNT_RESET_TOKEN_BLANK(1205, "Reset token is required", HttpStatus.BAD_REQUEST),
    ACCOUNT_RESET_TOKEN_INVALID(1206, "Reset link is invalid or has expired", HttpStatus.BAD_REQUEST),
    ACCOUNT_PASSWORD_SAME_AS_OLD(1207, "New password must differ from the old password", HttpStatus.BAD_REQUEST),
    ACCOUNT_CANT_BE_LOCKED(1208, "Super admin account cannot be locked", HttpStatus.BAD_REQUEST),

    // Store (1301 - 1400)
    STORE_NOT_FOUND(1301, "Store not found", HttpStatus.NOT_FOUND),
    STORE_ASSIGN_NON_ADMIN_ACCOUNT(1302, "Only a store admin account can be assigned to a store",
            HttpStatus.BAD_REQUEST),
    STORE_ADMIN_ASSIGNED(1303, "Admin has already been assigned to another store", HttpStatus.BAD_REQUEST),
    STORE_ADMIN_ID_REQUIRED(1304, "Store admin id is required", HttpStatus.BAD_REQUEST),
    STORE_NAME_BLANK(1305, "Store name is required", HttpStatus.BAD_REQUEST),
    STORE_ADDRESS_BLANK(1306, "Store address is required", HttpStatus.BAD_REQUEST),
    STORE_LOCATION_INFO_BLANK(1307, "Store location info is required", HttpStatus.BAD_REQUEST),
    STORE_ADMIN_BLANK(1308, "Store admin id is required", HttpStatus.BAD_REQUEST),
    STORE_INVENTORY_EXISTED(1309, "Store still holds inventory", HttpStatus.BAD_REQUEST),
    STORE_NO_CENTRAL(1310, "Central store not found; set is_central = true on exactly one store",
            HttpStatus.INTERNAL_SERVER_ERROR),

    // Category (1401 - 1500)
    CATEGORY_NAME_REQUIRED(1401, "Category name is required", HttpStatus.BAD_REQUEST),
    CATEGORY_DESCRIPTION_REQUIRED(1402, "Category description is required", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1403, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_PRODUCT_EXISTED(1404, "Category still contains products", HttpStatus.BAD_REQUEST),

    // Product (1501 - 1600)
    PRODUCT_NOT_FOUND(1501, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_CATEGORY_BLANK(1502, "Category is required", HttpStatus.BAD_REQUEST),
    PRODUCT_BRAND_BLANK(1503, "Brand is required", HttpStatus.BAD_REQUEST),
    PRODUCT_NAME_BLANK(1504, "Product name is required", HttpStatus.BAD_REQUEST),
    PRODUCT_VARIANTS_REQUIRED(1505, "At least one variant is required", HttpStatus.BAD_REQUEST),
    PRODUCT_SLUG_BLANK(1506, "Product slug is required", HttpStatus.BAD_REQUEST),
    PRODUCT_SLUG_EXISTED(1507, "Slug is already in use", HttpStatus.CONFLICT),
    PRODUCT_RELATED_MUST_BE_ROOT(1508, "Related product must be a root product", HttpStatus.BAD_REQUEST),
    PRODUCT_HAS_RELATED_CHILDREN(1509, "Product still has related child versions", HttpStatus.CONFLICT),

    // Brand (1601 - 1700)
    BRAND_NAME_BLANK(1601, "Brand name is required", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND(1602, "Brand not found", HttpStatus.NOT_FOUND),
    BRAND_PRODUCT_EXISTED(1603, "Remove all products belonging to this brand first", HttpStatus.BAD_REQUEST),

    // Size (1701 - 1800)
    SIZE_NAME_BLANK(1701, "Size name is required", HttpStatus.BAD_REQUEST),
    SIZE_TYPE_BLANK(1702, "Size type is required", HttpStatus.BAD_REQUEST),
    SIZE_NOT_FOUND(1703, "Size not found", HttpStatus.NOT_FOUND),
    SIZE_VARIANT_EXISTED(1704, "Remove all product variants using this size first", HttpStatus.BAD_REQUEST),

    // Color (1801 - 1900)
    COLOR_NAME_BLANK(1801, "Color name is required", HttpStatus.BAD_REQUEST),
    COLOR_NOT_FOUND(1802, "Color not found", HttpStatus.BAD_REQUEST),
    COLOR_VARIANT_EXISTED(1803, "Remove all product variants using this color first", HttpStatus.BAD_REQUEST),

    // Product variant (1901 - 2000)
    VARIANT_NOT_FOUND(1901, "Product variant not found", HttpStatus.NOT_FOUND),
    VARIANT_COLOR_BLANK(1902, "Color is required", HttpStatus.BAD_REQUEST),
    VARIANT_PRICE_BLANK(1903, "Price is required", HttpStatus.BAD_REQUEST),
    VARIANT_PRICE_MUST_BE_POSITIVE(1904, "Price must be greater than 0", HttpStatus.BAD_REQUEST),
    VARIANT_DUPLICATE_COMBINATION(1905, "This color/size combination already exists for the product",
            HttpStatus.CONFLICT),
    VARIANT_SKU_BLANK(1906, "SKU is required", HttpStatus.BAD_REQUEST),
    VARIANT_SKU_EXISTED(1907, "SKU is already in use", HttpStatus.CONFLICT),
    VARIANT_IN_USE(1908,
            "Cannot delete a variant that has inventory or orders; remove the related inventory first",
            HttpStatus.CONFLICT),

    // Product specification (2001 - 2100)
    SPEC_NAME_REQUIRED(2001, "Specification name is required", HttpStatus.BAD_REQUEST),
    SPEC_VALUE_REQUIRED(2002, "Specification value is required", HttpStatus.BAD_REQUEST),

    // Variant image (2101 - 2200)
    VARIANT_IMAGE_NOT_FOUND(2101, "Image not found", HttpStatus.NOT_FOUND),
    VARIANT_IMAGE_PUBLIC_ID_DUPLICATE(2102, "This image is already used for another variant", HttpStatus.CONFLICT),

    // Inventory (2201 - 2300)
    INVENTORY_NOT_FOUND(2201, "Inventory not found", HttpStatus.NOT_FOUND),
    INVENTORY_VARIANT_NOT_FOUND(2202, "Variant has no inventory in this store", HttpStatus.BAD_REQUEST),
    INVENTORY_STORE_ID_REQUIRED(2203, "Store ID is required", HttpStatus.BAD_REQUEST),
    INVENTORY_VARIANT_ID_REQUIRED(2204, "Variant ID is required", HttpStatus.BAD_REQUEST),
    INVENTORY_QUANTITY_REQUIRED(2205, "Quantity is required", HttpStatus.BAD_REQUEST),
    INVENTORY_QUANTITY_NEGATIVE(2206, "Quantity must not be negative", HttpStatus.BAD_REQUEST),
    INVENTORY_FORBIDDEN(2207, "Not allowed to update this inventory", HttpStatus.FORBIDDEN),

    // Order (2301 - 2400)
    ORDER_NOT_FOUND(2301, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_INVENTORY_INSUFFICIENT(2302, "Insufficient stock", HttpStatus.BAD_REQUEST),
    ORDER_INVALID_STATUS(2303, "Invalid order status", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_CANCEL(2304, "Order cannot be cancelled in its current status", HttpStatus.BAD_REQUEST),
    ORDER_FORBIDDEN(2305, "Not allowed to access this order", HttpStatus.FORBIDDEN),
    ORDER_ITEMS_REQUIRED(2306, "Order must have at least one item", HttpStatus.BAD_REQUEST),
    ORDER_RECIPIENT_NAME_REQUIRED(2307, "Recipient name is required", HttpStatus.BAD_REQUEST),
    ORDER_RECIPIENT_PHONE_REQUIRED(2308, "Recipient phone is required", HttpStatus.BAD_REQUEST),
    ORDER_RECIPIENT_ADDRESS_REQUIRED(2309, "Recipient address is required", HttpStatus.BAD_REQUEST),
    ORDER_RECIPIENT_EMAIL_INVALID(2310, "Invalid recipient email", HttpStatus.BAD_REQUEST),
    ORDER_PAYMENT_METHOD_REQUIRED(2311, "Payment method is required", HttpStatus.BAD_REQUEST),
    ORDER_SHIPPING_CODE_REQUIRED(2313, "Shipping code is required", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_NOT_FOUND(2314, "Order item not found", HttpStatus.NOT_FOUND),
    ORDER_ITEM_VARIANT_REQUIRED(2315, "Variant ID is required", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_QUANTITY_REQUIRED(2316, "Quantity is required", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_QUANTITY_INVALID(2317, "Quantity must be greater than 0", HttpStatus.BAD_REQUEST),

    // Payment (2401 - 2500)
    PAYMENT_NOT_FOUND(2401, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PAID(2402, "Order is already paid", HttpStatus.BAD_REQUEST),
    PAYMENT_ORDER_ID_REQUIRED(2403, "Order ID is required", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_SIGNATURE(2404, "Invalid payment signature", HttpStatus.BAD_REQUEST),

    // Review (2501 - 2600)
    REVIEW_RATING_OUT_OF_RANGE(2501, "Rating must be between 1 and 5", HttpStatus.BAD_REQUEST),
    REVIEW_RATING_BLANK(2502, "Rating is required", HttpStatus.BAD_REQUEST),
    REVIEW_ORDER_ITEM_BLANK(2503, "Order item id is required", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(2504, "You have already reviewed this order item", HttpStatus.CONFLICT),

    // Resource (2601 - 2700)
    RESOURCE_NOT_FOUND(2601, "Resource not found", HttpStatus.NOT_FOUND),
    RESOURCE_REORDER_INVALID(2602, "Reorder list does not match existing resources", HttpStatus.BAD_REQUEST),

    // Database constraint violations (9001 - 9100)
    DATABASE_CONSTRAINT_VIOLATION(9001, "Data constraint violation", HttpStatus.BAD_REQUEST),
    DATABASE_DUPLICATE_KEY(9002, "Record already exists", HttpStatus.CONFLICT),
    DATABASE_FOREIGN_KEY_VIOLATION(9003, "Foreign key constraint violation", HttpStatus.BAD_REQUEST),
    DATABASE_UNIQUE_CONSTRAINT_VIOLATION(9004, "Duplicate data", HttpStatus.CONFLICT),

    // File errors (9101 - 9200)
    FILE_EMPTY(9101, "File must not be empty", HttpStatus.BAD_REQUEST),
    FILE_TYPE_NOT_SUPPORTED(9102, "File type is not supported", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(9103, "File upload failed", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(9104, "File size exceeds the maximum allowed limit", HttpStatus.BAD_REQUEST),

    // Data errors (9201 - 9300)
    ENUM_INVALID_VALUE(9201, "Invalid enum value", HttpStatus.BAD_REQUEST),

    // System errors (9901 - 9999)
    SYSTEM_UNKNOWN_ERROR(9998, "System unknown error", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_INTERNAL_ERROR(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private int code;
    private String message;
    private HttpStatus status;
}
