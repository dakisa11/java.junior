# Managing Restoraunt Application

A web application for managing buyers and their orders.

The application allows:
- buyer managment
- order creation and viewing
- adding order items from predefined menu
- automatic total price calculation
- order status updates
- viewing orders per buyer
- basic authentication using Spring Security

---

## Technology Stack
- **Java 17**
- **Spring Boot 3**
- **Spring MVC**
- **Spring Data JDBC**
- **Spring Security**
- **H2 in-memory database**
- **Thymeleaf**
- **Bootstrap 4**
- **JavaScript (external script for price calculation)**

---

## Project Structure
```
src/main/java
└── hr.abysalto.hiring.api.junior
├── controller # MVC controllers
├── manager # Business logic layer
├── repository # Spring Data JDBC repositories
├── model # Domain models (Order, Buyer, OrderItem, etc.)
├── dto # DTOs for view rendering
├── Service # MenuService and helper logic
├── components # DatabaseInitializer
└── configuration # Spring Security configuration

src/main/resources
├── templates # Thymeleaf templates
├── static/js # JavaScript files (order-form.js)
└── application.properties
```
---

## Running the Application

### Prerequisites
- Java 17 or newer
- Maven 3.8+

### Run the application
```bash

mvn spring-boot:run
```

The application will start on: http://localhost:8080

---

## Login
Application uses in-memory auth

Credidentals: 
- username: user
- password: password

After login, dummy data is initialized and user is redirected to main application page /buyer

---

## Database
- H2 in-memory database
- Tables created at startup
- Dummy data loaded using DatabaseInitializer
- Data is reset on every application start

---

## Features

### Buyers
- list all buyers
- create, update and delete buyers
- view order for specific buyer

### Orders
- create new orders
- select buyer and delivery address
- add items from a predefined menu
- automatic total price calculation
- prices in multiple currencies (exchange included)
- update order status
- edit existing orders
- delete orders

## Menu Items
- predefined in MenuService
- prices defined in EUR
  
---
