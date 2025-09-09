# Smart Taskhub - Backend

This is the **backend service** for Smart Taskhub, built with **Play Framework (Scala)** and **PostgreSQL**.

---

## ⚙️ Tech Stack
- **Language:** Scala
- **Framework:** Play Framework (3.0.8)
- **Database:** PostgreSQL
- **Build Tool:** sbt

---

## 🛠 Setup Instructions
### 1. Install Dependencies
Make sure you have:
- **Java 18+**
- **sbt**
- **PostgreSQL**

### 2. Create Database
Make sure PostgreSQL is running and create the database:
```bash
psql -U your_user -c "CREATE DATABASE smart_taskhub;"
```
Or using createdb:
```bash
createdb -U your_user smart_taskhub
```

### 3. Configure Environment
Update `application.conf` or use environment variables:
```bash
DB_URL=jdbc:postgresql://localhost:5432/smart_taskhub
DB_USER=your_user
DB_PASSWORD=your_password
```

### 4. Run the App
```bash
sbt run
```

### 5. Run Tests
```bash
sbt test
```

### 6. Code Quality
```bash
sbt scalastyle
```
## 📂 Project Structure
```bash
backend/
│
├── api-specs/ # API documentation (Swagger/OpenAPI)
├── app/
│ ├── controllers/ # HTTP controllers
│ ├── db/ # Database configuration and migrations
│ ├── dto/ # Data Transfer Objects
│ ├── exception/ # Exception handling
│ ├── filters/ # Request/response filters
│ ├── init/ # App initialization logic
│ ├── mappers/ # Entity ↔ DTO mappers
│ ├── models/ # Domain models
│ ├── modules/ # Dependency injection modules
│ ├── repositories/ # Data access layer
│ ├── services/ # Business logic
│ ├── utils/ # Utility functions
│ └── validations/ # Custom validations
├── conf/ # Config files (application.conf, routes)
├── postman_collections/ # API collections for testing
├── project/ # sbt project settings
├── public/ # Static assets
├── test/ # Unit and integration tests
├── build.sbt # sbt build file
└── Dockerfile # Docker image definition
```