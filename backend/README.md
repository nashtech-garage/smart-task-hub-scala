# Smart Taskhub - Backend

This is the **backend service** for Smart Taskhub, built with **Play Framework (Scala)** and **PostgreSQL**.

---

## Tech Stack
- **Language:** Scala 2
- **Framework:** Play Framework (3.0.8)
- **Database:** PostgreSQL
- **Build Tool:** sbt
- **ORM / Database Access:** Slick
- **Caching:** Caffein
- **WebSocket:** Akka Actors
- **Testing:** ScalaTest

---

## Setup Instructions
### 1. Install Dependencies
Make sure you have:
- **JDK 11+**
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
The backend uses default database configuration values defined in
`conf/application.conf`.

If your database settings are different, create a `.env` file in this backend folder (same level as this README) and add the following lines:

```bash
DB_URL=your_url
DB_USER=your_user
DB_PASSWORD=your_password
```
The `.env` file overrides the default values in `application.conf`, allowing you to customize your local setup without modifying the configuration file directly.

### 4. Run the App
```bash
sbt run
```

### 5. Code Quality
```bash
sbt scalastyle
```

### 6. Run Tests
```bash
sbt test
```

### 7. Test Coverage
Generate a code coverage report:
```bash
sbt clean coverage test coverageReport
```
After running the command, open the coverage report located at:
```bash
target/scala-2.13/scoverage-report/index.html
```
You can open it in your browser to view detailed coverage metrics for each package and class.
## Project Structure
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
│ ├── actors/ # Akka actors for managing WebSocket connections and messaging
│ ├── cache/ # Cache management and caching utilities
│ ├── constants/ # Application-wide constant values and configuration keys
│ └── validations/ # Custom validations
├── conf/ # Config files (application.conf, routes)
├── postman_collections/ # API collections for testing
├── project/ # sbt project settings
├── public/ # Static assets
├── test/ # Unit and integration tests
├── build.sbt # sbt build file
└── Dockerfile # Docker image definition
```