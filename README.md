# 🏸 Goodminton Shop API

A comprehensive e-commerce REST API for badminton equipment built with Spring Boot, providing complete product management, user authentication, and order processing capabilities.

## 🚀 Technologies Used

### Backend Framework
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Database & Caching
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)

### Authentication & Security
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)
![OAuth 2.0](https://img.shields.io/badge/OAuth%202.0-3C5BBA?style=for-the-badge&logo=oauth&logoColor=white)

### Cloud Services
![Cloudinary](https://img.shields.io/badge/Cloudinary-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white)

### Development Tools
![Java](https://img.shields.io/badge/Java%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-BC4C00?style=for-the-badge&logo=lombok&logoColor=white)

### API Documentation & Validation
![Hibernate Validator](https://img.shields.io/badge/Hibernate%20Validator-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

## 📋 Project Overview

The Goodminton Shop API is a robust e-commerce backend system specifically designed for badminton equipment stores. It provides comprehensive functionality for managing products, handling user authentication, processing orders, and maintaining inventory across multiple store locations.

### 🎯 Key Features

- **🔐 Multi-Role Authentication System** (Super Admin, Store Admin, Customer)
- **🛍️ Complete Product Management** with variants, specifications, and images
- **📦 Inventory Management** across multiple store locations
- **🛒 Order Processing** with payment integration
- **⭐ Review & Rating System**
- **🏪 Multi-Store Support**
- **💾 Redis Caching** for performance optimization
- **☁️ Cloud Image Storage** with Cloudinary

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/com/lezh1n/goodminton_shop_api/
│   │   ├── configurations/     # Security, Redis, Cloudinary configs
│   │   ├── controllers/        # REST API endpoints
│   │   ├── dtos/              # Request/Response DTOs
│   │   │   ├── request/       # API request models
│   │   │   └── response/      # API response models
│   │   ├── entities/          # JPA entities
│   │   ├── enums/             # Application enums
│   │   ├── exceptions/        # Custom exceptions & error handling
│   │   ├── mappers/           # Entity-DTO mappers
│   │   ├── repositories/      # Data access layer
│   │   └── services/          # Business logic layer
│   └── resources/
│       ├── application.yaml   # Configuration
│       └── db/migration/      # Flyway database migrations
└── test/                      # Test files
```

## 🗄️ Database Schema

### Core Entities

#### 👤 User Management
- **Account** - User accounts with role-based access
- **Store** - Physical store locations with admin assignments

#### 🛍️ Product Catalog
- **Product** - Main product information
- **Category** - Product categories
- **Brand** - Product brands
- **ProductSpecification** - Technical specifications
- **ProductVariant** - Product variants (color, version)
- **Size** - Available sizes (Racket, Costume, Footwear)
- **VariantSize** - Size-specific pricing
- **VariantImage** - Product images

#### 📦 Inventory & Orders
- **Inventory** - Stock levels per store
- **Order** - Customer orders
- **OrderItem** - Individual order items
- **Payment** - Payment transactions
- **Review** - Product reviews and ratings

### 🔧 Enums
- **UserRole**: `SUPER_ADMIN`, `STORE_ADMIN`, `CUSTOMER`
- **OrderStatus**: `NEW`, `PAID`, `SHIPPED`, `COMPLETED`, `CANCEL`
- **PaymentMethod**: `COD`, `BANKING`, `VNPAY`
- **SizeType**: `RACKET`, `COSTUME`, `FOOTWEAR`

## 🚀 API Endpoints

### 🔐 Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Token refresh
- `POST /api/auth/logout` - User logout

### 🛍️ Product Management
- `GET /api/products` - List all products (paginated)
- `GET /api/products/{id}` - Get product details
- `POST /api/products` - Create product (Admin only)
- `PUT /api/products/{id}` - Update product (Admin only)
- `DELETE /api/products/{id}` - Delete product (Admin only)

### 📝 Product Specifications
- `POST /api/products/{id}/specifications` - Add specification
- `DELETE /api/products/{id}/specification/{specId}` - Remove specification

### 🎨 Product Variants
- `POST /api/products/{id}/variants` - Add variant
- `DELETE /api/products/{id}/variants/{variantId}` - Remove variant

### 🏷️ Catalog Management
- `GET /api/categories` - List categories
- `GET /api/brands` - List brands
- `GET /api/versions` - List versions
- `GET /api/colors` - List colors
- `GET /api/sizes` - List sizes

## ⚙️ Configuration

### Environment Variables
Create a .env file in the root directory:

```env
# Database Configuration
POSTGRES_PORT=db_port
POSTGRES_USERNAME=your_username
POSTGRES_PASSWORD=your_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET_KEY=your_jwt_secret_key
JWT_ISSUER=goodminton-api
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=86400000

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### Application Configuration
The application uses `application.yaml` for configuration with environment variable substitution.

## 🛡️ Security Features

### Authentication & Authorization
- **JWT-based authentication** with access and refresh tokens
- **Role-based access control** (RBAC)
- **Token blacklisting** using Redis
- **Password encryption** with BCrypt

### API Security
- **CORS configuration** for cross-origin requests
- **Input validation** with Bean Validation
- **Custom exception handling** with standardized error responses
- **Method-level security** with `@PreAuthorize`

## 🎯 Business Logic Highlights

### Product Management
- **Complex product variants** with size-specific pricing
- **Image management** with Cloudinary integration
- **Specification system** for technical details
- **Category and brand organization**

### Inventory System
- **Multi-store inventory tracking**
- **Real-time stock updates**
- **Discount management** with time-based pricing

### Order Processing
- **Complete order lifecycle management**
- **Multiple payment methods** support
- **Customer information handling**
- **Order status tracking**

## 🚀 Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Redis 6+
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/goodminton-shop-api.git
   cd goodminton-shop-api
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start dependencies**
   ```bash
   # Start PostgreSQL and Redis
   docker-compose up -d postgres redis
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:8080`

## 📊 Database Migration

The project uses Flyway for database versioning:
- V1__init_schema.sql - Initial database schema
- `V2__init_super_admin.sql` - Default super admin user

Migrations run automatically on application startup.

## 🔍 API Response Format

All API responses follow a consistent format:

```json
{
  "code": 1000,
  "message": "Success",
  "result": {
    // Response data
  }
}
```

Error responses include appropriate HTTP status codes and error messages.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Contact

**Project Maintainer**: Lezh1n
**Email**: [hoangphilong1208@gmail.com]
**Project Link**: [https://github.com/good-min-ton/goodminton-shop-api.git](https://github.com/good-min-ton/goodminton-shop-api.git)

---

*Built with ❤️ for the badminton community*
