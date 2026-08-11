# E-commerce Backend Project

## Product Requirements Document

### Functional Requirements

#### 1. User Management
1.1 Registration: Users can register via email or social login.  
1.2 Login: Authenticated access with secure session handling.  
1.3 Profile Management: Users can update/view personal details.  
1.4 Password Recovery: Password reset via email-based token.  

#### 2. Product Catalog
2.1 Category Browsing: Products grouped by category.  
2.2 Product Details: Images, description, price, stock status.  
2.3 Search & Filter: Full-text search and filters (price, brand).  

#### 3. Cart & Checkout
3.1 Cart Management: Add/update/remove items.  
3.2 Cart Summary: Show total, discounts, taxes.  
3.3 Checkout Flow: Delivery address, payment method.  

#### 4. Orders
4.1 Confirmation: Show order summary and confirmation.  
4.2 History: List of previous orders with status.  
4.3 Tracking: Shipment tracking with updates.  

#### 5. Payments
5.1 Methods: Support credit cards, UPI, net banking.  
5.2 Security: PCI-compliant payment process.  
5.3 Receipts: Auto-generated receipts post-payment.  

#### 6. Security & Auth
6.1 Secure Sessions: Token-based (JWT) authentication.  
6.2 Session Timeout: Configurable expiry or manual logout.  

---

## High-Level Design (HLD)

### Components
- API Gateway (e.g., Kong or Spring Cloud Gateway)
- Load Balancer (AWS ELB or NGINX)
- Microservices
- Databases: MySQL, MongoDB
- Event Bus: Apache Kafka
- Cache: Redis
- Search Engine: Elasticsearch

### Microservices

#### 1. User Service
- Responsibilities: Auth, registration, profiles.  
- Events: `UserRegistered` → `WelcomeEmail`  

#### 2. Product Service
- Responsibilities: Catalog, search.  
- Features: Full-text search, fuzzy match.  

#### 3. Cart Service
- Responsibilities: Manage user's cart.  

#### 4. Order Service
- Responsibilities: Create order, track status.  

#### 5. Payment Service
- Responsibilities: Integrate with payment providers.  

#### 6. Notification Service
- Responsibilities: Send emails, SMS.  

---

## Typical Workflow with Kafka & Search

### Search
Request → API Gateway → Product Service → Elasticsearch

### Add to Cart
Cart Service saves → Kafka → CartActivityLogged

### Checkout
Order Service -> Kafka -> Payment Service → Order Updated