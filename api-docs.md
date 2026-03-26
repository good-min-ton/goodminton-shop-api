# Badminton Store System — API Documentation

## Conventions

**Base URL:** `http://localhost:8080/api/v1`

**Authentication:** JWT Bearer Token
```
Authorization: Bearer <token>
```

**Standard Response Envelope:**
```json
{
  "code": 1000,
  "result": { ... }
}
```

**Error Response:**
```json
{
  "code": 1001,
  "message": "Error description"
}
```

**Pagination (cho list endpoints):**
```json
{
  "code": 1000,
  "result": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

**Query params phân trang:** `?page=0&size=20&sort=createdAt,desc`

**Role Access:**
- `[PUBLIC]` — không cần token
- `[AUTH]` — đã đăng nhập (bất kỳ role)
- `[CUSTOMER]` — role CUSTOMER
- `[STORE_ADMIN]` — role STORE_ADMIN
- `[SUPER_ADMIN]` — role SUPER_ADMIN

---

## 1. Auth Service

### POST /auth/register `[PUBLIC]`
Đăng ký tài khoản khách hàng.

**Request:**
```json
{
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "phone": "0901234567",
  "password": "password123"
}
```

**Response `201`:**
```json
{
  "code": 1000,
  "message": "Đăng ký thành công",
  "result": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "phone": "0901234567",
    "role": "CUSTOMER"
  }
}
```

---

### POST /auth/login `[PUBLIC]`
Đăng nhập, trả về JWT.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "fullName": "Nguyen Van A",
      "email": "user@example.com",
      "role": "CUSTOMER"
    }
  }
}
```

---

### POST /auth/refresh-token `[PUBLIC]`
Làm mới access token.

**Request:**
```json
{
  "refreshToken": "eyJhbGci..."
}
```

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "accessToken": "eyJhbGci...",
    "expiresIn": 86400
  }
}
```

---

### POST /auth/logout `[AUTH]`
Vô hiệu hóa refresh token hiện tại.

**Response `200`:**
```json
{ "code": 1000,
```

---

### GET /auth/me `[AUTH]`
Lấy thông tin tài khoản đang đăng nhập.

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "phone": "0901234567",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

---

### PUT /auth/me `[AUTH]`
Cập nhật thông tin cá nhân.

**Request:**
```json
{
  "fullName": "Nguyen Van B",
  "phone": "0909999999"
}
```

---

### PUT /auth/change-password `[AUTH]`
Đổi mật khẩu.

**Request:**
```json
{
  "currentPassword": "oldpass",
  "newPassword": "newpass123",
  "confirmPassword": "newpass123"
}
```

---

## 2. Account Service (Super Admin)

### GET /admin/accounts `[SUPER_ADMIN]`
Lấy danh sách tài khoản (Store Admin + Customer).

**Query params:** `?role=STORE_ADMIN&status=ACTIVE&page=0&size=20`

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "content": [
      {
        "id": 2,
        "fullName": "Store Admin HCM",
        "email": "storehcm@example.com",
        "phone": "0901111111",
        "role": "STORE_ADMIN",
        "status": "ACTIVE",
        "store": {
          "id": 1,
          "name": "Chi nhánh HCM"
        },
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1
  }
}
```

---

### POST /admin/accounts `[SUPER_ADMIN]`
Tạo tài khoản Store Admin.

**Request:**
```json
{
  "fullName": "Store Admin HN",
  "email": "storehn@example.com",
  "phone": "0902222222",
  "password": "initpass123",
  "role": "STORE_ADMIN"
}
```

**Response `201`:**
```json
{
  "code": 1000,
  "result": {
    "id": 3,
    "fullName": "Store Admin HN",
    "email": "storehn@example.com",
    "role": "STORE_ADMIN",
    "status": "ACTIVE"
  }
}
```

---

### GET /admin/accounts/{id} `[SUPER_ADMIN]`
Lấy chi tiết một tài khoản.

---

### PUT /admin/accounts/{id} `[SUPER_ADMIN]`
Cập nhật thông tin tài khoản.

**Request:**
```json
{
  "fullName": "Store Admin HN Updated",
  "phone": "0903333333"
}
```

---

### PATCH /admin/accounts/{id}/status `[SUPER_ADMIN]`
Khoá / mở khoá tài khoản.

**Request:**
```json
{ "status": "INACTIVE" }
```

---

### DELETE /admin/accounts/{id} `[SUPER_ADMIN]`
Xoá tài khoản (soft delete nếu có).

---

## 3. Store Service

### GET /admin/stores `[SUPER_ADMIN]`
Lấy danh sách chi nhánh.

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "content": [
      {
        "id": 1,
        "name": "Chi nhánh HCM",
        "address": "123 Nguyen Hue, Q1, HCM",
        "contact": "0281234567",
        "longitude": 106.7008878,
        "latitude": 10.7763897,
        "admin": {
          "id": 2,
          "fullName": "Store Admin HCM"
        },
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "page": 0, "size": 20, "totalElements": 3, "totalPages": 1
  }
}
```

---

### POST /admin/stores `[SUPER_ADMIN]`
Tạo chi nhánh mới.

**Request:**
```json
{
  "name": "Chi nhánh Hà Nội",
  "address": "45 Tran Hung Dao, HN",
  "contact": "0241234567",
  "longitude": 105.8412,
  "latitude": 21.0245,
  "adminId": 3
}
```

---

### GET /admin/stores/{id} `[SUPER_ADMIN]`
Lấy chi tiết chi nhánh.

---

### PUT /admin/stores/{id} `[SUPER_ADMIN]`
Cập nhật thông tin chi nhánh.

---

### DELETE /admin/stores/{id} `[SUPER_ADMIN]`
Xoá chi nhánh.

---

### GET /store-admin/store `[STORE_ADMIN]`
Store Admin xem thông tin chi nhánh của mình.

---

## 4. Category Service

### GET /categories `[PUBLIC]`
Lấy danh sách danh mục (dùng cho frontend hiển thị menu).

**Response `200`:**
```json
{
  "code": 1000,
  "result": [
    { "id": 1, "name": "Vợt cầu lông", "description": "..." },
    { "id": 2, "name": "Giày cầu lông", "description": "..." }
  ]
}
```

---

### GET /categories/{id} `[PUBLIC]`
Lấy chi tiết danh mục.

---

### POST /admin/categories `[SUPER_ADMIN]`
Tạo danh mục.

**Request:**
```json
{
  "name": "Túi cầu lông",
  "description": "Các loại túi đựng vợt và dụng cụ"
}
```

---

### PUT /admin/categories/{id} `[SUPER_ADMIN]`
Cập nhật danh mục.

---

### DELETE /admin/categories/{id} `[SUPER_ADMIN]`
Xoá danh mục.

---

## 5. Brand Service

### GET /brands `[PUBLIC]`
Lấy danh sách thương hiệu.

**Response `200`:**
```json
{
  "code": 1000,
  "result": [
    { "id": 1, "name": "Yonex" },
    { "id": 2, "name": "Victor" },
    { "id": 3, "name": "Lining" }
  ]
}
```

---

### POST /admin/brands `[SUPER_ADMIN]`
Tạo thương hiệu.

**Request:**
```json
{ "name": "Kawasaki" }
```

---

### PUT /admin/brands/{id} `[SUPER_ADMIN]`
Cập nhật thương hiệu.

---

### DELETE /admin/brands/{id} `[SUPER_ADMIN]`
Xoá thương hiệu.

---

## 6. Color & Size Service

### GET /colors `[PUBLIC]`

**Response `200`:**
```json
{
  "code": 1000,
  "result": [
    { "id": 1, "name": "Đỏ", "hexCode": "#FF0000" },
    { "id": 2, "name": "Xanh dương", "hexCode": "#0000FF" }
  ]
}
```

---

### POST /admin/colors `[SUPER_ADMIN]`

**Request:**
```json
{ "name": "Vàng", "hexCode": "#FFD700" }
```

---

### PUT /admin/colors/{id} `[SUPER_ADMIN]`

---

### DELETE /admin/colors/{id} `[SUPER_ADMIN]`

---

### GET /sizes `[PUBLIC]`

**Response `200`:`**
```json
{
  "code": 1000,
  "result": [
    { "id": 1, "name": "4U", "type": "RACKET" },
    { "id": 2, "name": "3U", "type": "RACKET" },
    { "id": 3, "name": "40", "type": "NON_RACKET" }
  ]
}
```

---

### POST /admin/sizes `[SUPER_ADMIN]`

**Request:**
```json
{ "name": "5U", "type": "RACKET" }
```

---

### PUT /admin/sizes/{id} `[SUPER_ADMIN]`

---

### DELETE /admin/sizes/{id} `[SUPER_ADMIN]`

---

## 7. Product Service

### GET /products `[PUBLIC]`
Lấy danh sách sản phẩm cho trang listing.

**Query params:** `?categoryId=1&brandId=1&minPrice=100000&maxPrice=5000000&keyword=astrox&isVisible=true&page=0&size=20&sort=createdAt,desc`

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "content": [
      {
        "id": 1,
        "name": "Yonex Astrox 99",
        "slug": "yonex-astrox-99",
        "thumbnailUrl": "https://res.cloudinary.com/...",
        "category": { "id": 1, "name": "Vợt cầu lông" },
        "brand": { "id": 1, "name": "Yonex" },
        "minPrice": 3200000,
        "maxPrice": 3800000,
        "isVisible": true,
        "relatedProductId": null
      }
    ],
    "page": 0, "size": 20, "totalElements": 50, "totalPages": 3
  }
}
```

---

### GET /products/{slug} `[PUBLIC]`
Lấy chi tiết sản phẩm theo slug, kèm toàn bộ variants.

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "id": 1,
    "name": "Yonex Astrox 99",
    "slug": "yonex-astrox-99",
    "description": "...",
    "thumbnailUrl": "https://...",
    "category": { "id": 1, "name": "Vợt cầu lông" },
    "brand": { "id": 1, "name": "Yonex" },
    "specifications": [
      { "name": "Trọng lượng", "value": "85g" },
      { "name": "Độ cứng", "value": "Cứng" }
    ],
    "variants": [
      {
        "id": 10,
        "skuCode": "YNX-AX99-RED-4U",
        "price": 3200000,
        "discount": {
          "salePrice": 2900000,
          "endTime": "2024-12-31T23:59:59Z"
        },
        "color": { "id": 1, "name": "Đỏ", "hexCode": "#FF0000" },
        "size": { "id": 1, "name": "4U", "type": "RACKET" },
        "images": [
          {
            "id": 1,
            "imageUrl": "https://res.cloudinary.com/...",
            "sortOrder": 0
          }
        ]
      }
    ],
    "relatedProducts": [
      { "id": 2, "name": "Yonex Astrox 99 Play", "slug": "yonex-astrox-99-play", "thumbnailUrl": "..." }
    ]
  }
}
```

---

### POST /admin/products `[SUPER_ADMIN]`
Tạo sản phẩm mới.

**Request:**
```json
{
  "name": "Yonex Astrox 99",
  "categoryId": 1,
  "brandId": 1,
  "description": "Vợt tấn công hàng đầu...",
  "thumbnailUrl": "https://...",
  "slug": "yonex-astrox-99",
  "isVisible": true,
  "relatedProductId": null,
  "specifications": [
    { "name": "Trọng lượng", "value": "85g" }
  ]
}
```

**Response `201`:**
```json
{
  "code": 1000,
  "result": { "id": 1, "name": "Yonex Astrox 99", "slug": "yonex-astrox-99" }
}
```

---

### GET /admin/products `[SUPER_ADMIN]`
Danh sách sản phẩm cho admin (bao gồm cả is_visible = false).

**Query params:** `?categoryId=1&brandId=1&keyword=astrox&page=0&size=20`

---

### GET /admin/products/{id} `[SUPER_ADMIN]`
Chi tiết sản phẩm cho admin.

---

### PUT /admin/products/{id} `[SUPER_ADMIN]`
Cập nhật sản phẩm.

---

### PATCH /admin/products/{id}/visibility `[SUPER_ADMIN]`
Ẩn / hiện sản phẩm trên listing.

**Request:**
```json
{ "isVisible": false }
```

---

### DELETE /admin/products/{id} `[SUPER_ADMIN]`
Xoá sản phẩm.

---

## 8. Product Variant Service

### POST /admin/products/{productId}/variants `[SUPER_ADMIN]`
Thêm variant cho sản phẩm.

**Request:**
```json
{
  "colorId": 1,
  "sizeId": 1,
  "skuCode": "YNX-AX99-RED-4U",
  "price": 3200000
}
```

**Response `201`:**
```json
{
  "code": 1000,
  "result": {
    "id": 10,
    "skuCode": "YNX-AX99-RED-4U",
    "price": 3200000,
    "color": { "id": 1, "name": "Đỏ", "hexCode": "#FF0000" },
    "size": { "id": 1, "name": "4U" }
  }
}
```

---

### PUT /admin/variants/{variantId} `[SUPER_ADMIN]`
Cập nhật variant (giá, sku).

**Request:**
```json
{
  "skuCode": "YNX-AX99-RED-4U-V2",
  "price": 3500000
}
```

---

### DELETE /admin/variants/{variantId} `[SUPER_ADMIN]`
Xoá variant.

---

### POST /admin/variants/{variantId}/images `[SUPER_ADMIN]`
Upload ảnh cho variant (multipart/form-data, upload lên Cloudinary trước, sau đó gửi URL).

**Request:**
```json
{
  "publicId": "badminton/variants/ynx-ax99-red-4u-1",
  "imageUrl": "https://res.cloudinary.com/...",
  "sortOrder": 0
}
```

---

### DELETE /admin/variants/{variantId}/images/{imageId} `[SUPER_ADMIN]`
Xoá ảnh variant.

---

### PATCH /admin/variants/{variantId}/images/reorder `[SUPER_ADMIN]`
Đổi thứ tự ảnh.

**Request:**
```json
{
  "imageOrders": [
    { "imageId": 1, "sortOrder": 0 },
    { "imageId": 2, "sortOrder": 1 }
  ]
}
```

---

## 9. Product Discount Service

### POST /admin/variants/{variantId}/discounts `[SUPER_ADMIN]`
Tạo chương trình giảm giá cho variant.

**Request:**
```json
{
  "salePrice": 2900000,
  "startTime": "2024-12-01T00:00:00Z",
  "endTime": "2024-12-31T23:59:59Z",
  "isActive": true
}
```

---

### GET /admin/variants/{variantId}/discounts `[SUPER_ADMIN]`
Lấy danh sách discount của variant.

---

### PUT /admin/discounts/{discountId} `[SUPER_ADMIN]`
Cập nhật discount.

---

### PATCH /admin/discounts/{discountId}/status `[SUPER_ADMIN]`
Bật / tắt discount.

**Request:**
```json
{ "isActive": false }
```

---

### DELETE /admin/discounts/{discountId} `[SUPER_ADMIN]`
Xoá discount.

---

## 10. Inventory Service

### GET /admin/inventory `[SUPER_ADMIN]`
Xem tồn kho toàn hệ thống.

**Query params:** `?storeId=1&productId=1&variantId=10&lowStock=true&page=0&size=20`

`lowStock=true` — lọc các variant có quantity <= threshold (mặc định 5).

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "content": [
      {
        "id": 1,
        "store": { "id": 1, "name": "Chi nhánh HCM" },
        "variant": {
          "id": 10,
          "skuCode": "YNX-AX99-RED-4U",
          "product": { "id": 1, "name": "Yonex Astrox 99" },
          "color": { "name": "Đỏ" },
          "size": { "name": "4U" }
        },
        "quantity": 15,
        "updatedAt": "2024-01-15T10:00:00Z"
      }
    ],
    "page": 0, "size": 20, "totalElements": 100, "totalPages": 5
  }
}
```

---

### GET /store-admin/inventory `[STORE_ADMIN]`
Store Admin xem tồn kho chi nhánh của mình.

**Query params:** `?variantId=10&lowStock=true&page=0&size=20`

---

### PUT /admin/inventory `[SUPER_ADMIN]`
Super Admin cập nhật số lượng tồn kho (nhập hàng, điều chỉnh).

**Request:**
```json
{
  "storeId": 1,
  "variantId": 10,
  "quantity": 50
}
```

---

### PUT /store-admin/inventory `[STORE_ADMIN]`
Store Admin cập nhật tồn kho chi nhánh mình.

**Request:**
```json
{
  "variantId": 10,
  "quantity": 50
}
```

---

### PATCH /admin/inventory/adjust `[SUPER_ADMIN]`
Điều chỉnh tồn kho theo delta (cộng / trừ).

**Request:**
```json
{
  "storeId": 1,
  "variantId": 10,
  "delta": -5,
  "reason": "Hàng lỗi"
}
```

---

## 11. Order Service

### POST /orders `[CUSTOMER]`
Khách hàng đặt hàng online.

**Request:**
```json
{
  "items": [
    { "variantId": 10, "quantity": 2 },
    { "variantId": 11, "quantity": 1 }
  ],
  "recipientName": "Nguyen Van A",
  "recipientPhone": "0901234567",
  "recipientAddress": "123 Nguyen Hue, Q1, HCM",
  "recipientEmail": "user@example.com",
  "note": "Giao giờ hành chính",
  "paymentMethod": "VNPAY"
}
```

**Response `201`:**
```json
{
  "code": 1000,
  "result": {
    "id": 100,
    "status": "PENDING",
    "orderType": "ONLINE",
    "totalAmount": 9700000,
    "items": [
      {
        "id": 1,
        "variant": { "id": 10, "skuCode": "YNX-AX99-RED-4U" },
        "quantity": 2,
        "unitPrice": 3200000,
        "discountPrice": 2900000
      }
    ],
    "paymentUrl": "https://sandbox.vnpayment.vn/..."
  }
}
```

> `paymentUrl` chỉ có khi `paymentMethod = VNPAY`.

---

### GET /orders `[CUSTOMER]`
Lịch sử đơn hàng của khách hàng đang đăng nhập.

**Query params:** `?status=COMPLETED&page=0&size=10`

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "content": [
      {
        "id": 100,
        "status": "COMPLETED",
        "orderType": "ONLINE",
        "totalAmount": 9700000,
        "orderDate": "2024-01-10T08:00:00Z",
        "store": { "id": 1, "name": "Chi nhánh HCM" },
        "itemCount": 2
      }
    ],
    "page": 0, "size": 10, "totalElements": 5, "totalPages": 1
  }
}
```

---

### GET /orders/{id} `[CUSTOMER]`
Chi tiết đơn hàng (chỉ xem được đơn của chính mình).

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "id": 100,
    "status": "SHIPPING",
    "orderType": "ONLINE",
    "totalAmount": 9700000,
    "shippingCode": "GHN123456789",
    "recipientName": "Nguyen Van A",
    "recipientPhone": "0901234567",
    "recipientAddress": "123 Nguyen Hue, Q1, HCM",
    "note": "Giao giờ hành chính",
    "store": { "id": 1, "name": "Chi nhánh HCM", "address": "..." },
    "items": [
      {
        "id": 1,
        "variant": {
          "id": 10,
          "skuCode": "YNX-AX99-RED-4U",
          "product": { "id": 1, "name": "Yonex Astrox 99", "thumbnailUrl": "..." },
          "color": { "name": "Đỏ" },
          "size": { "name": "4U" }
        },
        "quantity": 2,
        "unitPrice": 3200000,
        "discountPrice": 2900000
      }
    ],
    "payment": {
      "id": 50,
      "method": "VNPAY",
      "status": "PAID",
      "amount": 9700000,
      "paidAt": "2024-01-10T08:05:00Z"
    },
    "orderDate": "2024-01-10T08:00:00Z"
  }
}
```

---

### PATCH /orders/{id}/cancel `[CUSTOMER]`
Huỷ đơn hàng (chỉ được khi status = PENDING).

**Request:**
```json
{ "reason": "Đặt nhầm sản phẩm" }
```

---

### POST /store-admin/orders `[STORE_ADMIN]`
Store Admin lên đơn tại quầy (POS).

**Request:**
```json
{
  "items": [
    { "variantId": 10, "quantity": 1 }
  ],
  "paymentMethod": "BANKING",
  "note": "Khách tự đến lấy"
}
```

> `orderType` tự động set là `IN_STORE`. `storeId` lấy từ thông tin Store Admin đang đăng nhập.

**Response `201`:**
```json
{
  "code": 1000,
  "result": {
    "id": 101,
    "status": "COMPLETED",
    "orderType": "IN_STORE",
    "totalAmount": 3200000
  }
}
```

> Đơn IN_STORE tự động chuyển `COMPLETED` ngay sau khi tạo.

---

### GET /admin/orders `[SUPER_ADMIN]`
Super Admin xem toàn bộ đơn hàng.

**Query params:** `?status=PENDING&orderType=ONLINE&storeId=1&fromDate=2024-01-01&toDate=2024-01-31&page=0&size=20`

---

### GET /store-admin/orders `[STORE_ADMIN]`
Store Admin xem đơn hàng của chi nhánh mình.

**Query params:** `?status=PREPARING&orderType=ONLINE&page=0&size=20`

---

### GET /admin/orders/{id} `[SUPER_ADMIN]`
Super Admin xem chi tiết bất kỳ đơn hàng.

---

### PATCH /admin/orders/{id}/status `[SUPER_ADMIN]`
Super Admin cập nhật trạng thái đơn hàng + assign store.

**Request:**
```json
{
  "status": "CONFIRMED",
  "storeId": 1
}
```

---

### PATCH /store-admin/orders/{id}/status `[STORE_ADMIN]`
Store Admin cập nhật trạng thái đơn hàng của chi nhánh mình.

**Request:**
```json
{
  "status": "PREPARING"
}
```

> Store Admin chỉ được phép chuyển: `CONFIRMED → PREPARING → SHIPPING`. Không được tự CANCEL hay COMPLETED với đơn ONLINE.

---

### PATCH /admin/orders/{id}/shipping `[SUPER_ADMIN]`
Cập nhật mã vận đơn sau khi tạo đơn với GHN/GHTK.

**Request:**
```json
{
  "shippingCode": "GHN123456789"
}
```

---

## 12. Payment Service

### GET /vnpay/create-payment-url `[CUSTOMER]`
Tạo URL thanh toán VNPay cho đơn hàng (thường được gọi ngay sau khi tạo order).

**Query params:** `?orderId=100`

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
  }
}
```

---

### GET /vnpay/callback `[PUBLIC]`
VNPay redirect về sau khi thanh toán. Spring Boot xử lý verify signature, cập nhật `payment.status` và `order.status`.

**Query params:** Các params chuẩn của VNPay (`vnp_ResponseCode`, `vnp_TxnRef`, `vnp_TransactionNo`, ...).

> Endpoint này redirect về frontend với query param `?success=true&orderId=100` hoặc `?success=false`.

---

### GET /vnpay/ipn `[PUBLIC]`
VNPay IPN (Instant Payment Notification) — webhook server-to-server. Spring Boot verify và cập nhật trạng thái thanh toán.

> Response theo đúng format VNPay yêu cầu: `{"RspCode":"00","Message":"Confirm Success"}`

---

### GET /admin/payments `[SUPER_ADMIN]`
Lịch sử thanh toán toàn hệ thống.

**Query params:** `?method=VNPAY&status=PAID&fromDate=2024-01-01&toDate=2024-01-31&page=0&size=20`

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "content": [
      {
        "id": 50,
        "order": { "id": 100, "totalAmount": 9700000 },
        "method": "VNPAY",
        "status": "PAID",
        "amount": 9700000,
        "vnpayTxnRef": "ORDER_100_1704844800",
        "vnpayTransactionNo": "14052183",
        "vnpayBankCode": "NCB",
        "paidAt": "2024-01-10T08:05:00Z"
      }
    ],
    "page": 0, "size": 20, "totalElements": 200, "totalPages": 10
  }
}
```

---

### GET /store-admin/payments `[STORE_ADMIN]`
Store Admin xem lịch sử thanh toán chi nhánh mình.

---

## 13. Review Service

### POST /reviews `[CUSTOMER]`
Đăng review. Chỉ review được sau khi đơn `COMPLETED`.

**Request:**
```json
{
  "orderItemId": 1,
  "rating": 5,
  "comment": "Sản phẩm tốt, giao hàng nhanh!"
}
```

> Backend tự lấy `productId` từ `orderItemId`. Validate đơn hàng phải ở trạng thái `COMPLETED` và thuộc về customer đang đăng nhập.

---

### GET /products/{productId}/reviews `[PUBLIC]`
Lấy danh sách review của sản phẩm.

**Query params:** `?rating=5&page=0&size=10`

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "summary": {
      "averageRating": 4.7,
      "totalReviews": 128,
      "distribution": {
        "5": 90, "4": 25, "3": 8, "2": 3, "1": 2
      }
    },
    "content": [
      {
        "id": 1,
        "customer": { "fullName": "Nguyen V***" },
        "rating": 5,
        "comment": "Sản phẩm tốt!",
        "variant": { "color": { "name": "Đỏ" }, "size": { "name": "4U" } },
        "createdAt": "2024-01-15T10:00:00Z"
      }
    ],
    "page": 0, "size": 10, "totalElements": 128, "totalPages": 13
  }
}
```

---

### DELETE /admin/reviews/{id} `[SUPER_ADMIN]`
Xoá review vi phạm.

---

## 14. Dashboard / Report Service

### GET /admin/dashboard `[SUPER_ADMIN]`
Thống kê tổng quan toàn hệ thống.

**Query params:** `?period=today|week|month|year`

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "totalRevenue": 150000000,
    "totalOrders": 320,
    "totalOrdersOnline": 250,
    "totalOrdersInStore": 70,
    "pendingOrders": 15,
    "revenueByStore": [
      { "store": { "id": 1, "name": "Chi nhánh HCM" }, "revenue": 90000000 },
      { "store": { "id": 2, "name": "Chi nhánh HN" }, "revenue": 60000000 }
    ],
    "topProducts": [
      { "product": { "id": 1, "name": "Yonex Astrox 99" }, "soldQuantity": 45 }
    ],
    "revenueChart": [
      { "date": "2024-01-01", "revenue": 5000000 },
      { "date": "2024-01-02", "revenue": 7000000 }
    ]
  }
}
```

---

### GET /store-admin/dashboard `[STORE_ADMIN]`
Thống kê cho chi nhánh hiện tại.

**Query params:** `?period=today|week|month|year`

**Response `200`:**
```json
{
  "code": 1000,
  "result": {
    "totalRevenue": 90000000,
    "totalOrders": 180,
    "pendingOrders": 8,
    "lowStockCount": 5,
    "revenueChart": [ ... ]
  }
}
```

---

## 15. Service Layer — Spring Boot Structure

```
com.badminton.store
├── controller
│   ├── AuthController
│   ├── admin
│   │   ├── AccountController
│   │   ├── StoreController
│   │   ├── ProductController
│   │   ├── VariantController
│   │   ├── DiscountController
│   │   ├── InventoryController
│   │   ├── OrderController
│   │   ├── PaymentController
│   │   ├── ReviewController
│   │   └── DashboardController
│   ├── storeAdmin
│   │   ├── StoreInfoController
│   │   ├── InventoryController
│   │   ├── OrderController
│   │   └── DashboardController
│   └── customer
│       ├── ProductController
│       ├── OrderController
│       ├── PaymentController
│       └── ReviewController
│
├── service
│   ├── AuthService
│   ├── AccountService
│   ├── StoreService
│   ├── CategoryService
│   ├── BrandService
│   ├── ColorService
│   ├── SizeService
│   ├── ProductService
│   ├── VariantService
│   ├── DiscountService
│   ├── InventoryService
│   ├── OrderService
│   ├── OrderAllocationService    ← logic chọn kho, tạo allocation
│   ├── PaymentService
│   ├── VNPayService              ← tạo URL, verify callback, xử lý IPN
│   └── ReviewService
│
├── repository (JpaRepository)
├── entity
├── dto
│   ├── request
│   └── response
├── mapper                        ← MapStruct
├── exception
│   ├── GlobalExceptionHandler
│   ├── ResourceNotFoundException
│   ├── BusinessException
│   └── UnauthorizedException
├── security
│   ├── JwtTokenProvider
│   ├── JwtAuthenticationFilter
│   └── SecurityConfig
└── config
    ├── VNPayConfig
    └── CloudinaryConfig
```

---

## 16. HTTP Status Codes

| Code | Ý nghĩa |
|------|---------|
| `200` | Thành công |
| `201` | Tạo mới thành công |
| `400` | Request không hợp lệ (validation fail) |
| `401` | Chưa xác thực (thiếu / sai token) |
| `403` | Không có quyền |
| `404` | Không tìm thấy resource |
| `409` | Conflict (duplicate slug, sku,...) |
| `422` | Logic nghiệp vụ thất bại (hết hàng, không đủ điều kiện,...) |
| `500` | Lỗi server |
