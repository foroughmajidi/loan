# Loan System API

## Overview
A **Java-based loan management system** developed using Spring Boot, Docker, PostgreSQL, Swagger, and Maven.

## Features
 **User Authentication**
- Register new users
- Login and obtain JWT tokens

 **Loan Management (Admin Only)**
- Create, update, and delete loans
- Accept or reject loan requests

 **Loan Requests**
- Request a loan
- Cancel a loan request

 **Dockerized Deployment**
- Deployable using Docker with PostgreSQL as the database.

## Technologies Used
- **Spring Boot 3.3.2**
- **JDK 21**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Maven**
- **Swagger (OpenAPI 3)**

---

## Setup & Installation
### **1. Clone the Repository**

git clone https://github.com/foroughmajidi/loan.git
cd loan/loan-system

### **2. Run with Docker**
Ensure Docker is installed, then run:
docker-compose up -d

### **3. Manual Setup (Without Docker)**
- Configure PostgreSQL in application.properties
- Build and run the app
  
- The application will be accessible at **http://localhost:8080**.

## API Documentation

### **Authentication Endpoints**
| Method | Endpoint             | Description |
|--------|----------------------|-----------------------------------|
| POST   | /api/auth/register   | Register a new user               |
| POST   | /api/auth/login      | Authenticate and obtain JWT token |

### **Loan Request Endpoints**
| Method | Endpoint                                          | Description                             |                                 
|--------|---------------------------------------------------|-----------------------------------------|
| POST   | /api/loan-requests/requestLoan                    | Submit a loan request                   |
| PUT    | /api/loan-requests/cancelLoanRequest/{id}         | Cancel a loan request                   |
| PUT    | /api/loan-requests/accept/{loanRequestId}         | Accept a loan request  (Admin Only)     |
| PUT    | /api/loan-requests/reject/{loanRequestId}         | Reject a loan request  (Admin Only)     |

### **Loan Management Endpoints**
| Method | Endpoint                         | Description                             |
|--------|----------------------------------|-----------------------------------------|
| POST   |  /api/loans/create               | Create a new loan      (Admin Only)     |
| GET    |  /api/loans/{id}                 | Get loan by ID                          |
| GET    |  /api/loans/findLoans            | Get all loans                           |
| PUT    |  /api/loans/updateLoan/{id}      | Update a loan          (Admin Only)     |
| DELETE |  /api/loans/deleteLoan/{id}      | Delete a loan          (Admin Only)     |
| GET    |  /api/loans/loanNames            | Get available loan                 |

---

## Security
This API uses **JWT (JSON Web Tokens)** for authentication.
  **Bearer Token Format:** Include the token in the "Authorization" header:
  Authorization: Bearer your_jwt_token_here
 **Admin Role Required:** Some endpoints require ADMIN role. Ensure the token belongs to an admin.

## Running Tests
### **Unit Tests & Integration Tests**
The project includes **JUnit-based unit tests and integration tests**.

#### **Run all tests:**
mvn test

## Swagger API Documentation
Once the application is running, access **Swagger UI** at:
 -http://localhost:8080/swagger-ui/index.html
 - http://localhost:8080/swagger-ui.html
 - http://localhost:8080/v3/api-docs


## Contributors
- **Forough Majidi** ([@foroughmajidi](https://github.com/foroughmajidi))

Feel free to contribute by submitting issues or pull requests!
