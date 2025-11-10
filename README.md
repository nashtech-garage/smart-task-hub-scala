# Smart Taskhub

**Smart Taskhub** is a project management application that helps teams organize tasks, manage workflows, and collaborate effectively.

## Technologies and Frameworks
- **Frontend:** React, TypeScript, Vite, Tailwind CSS, Redux Toolkit, TanStack (React Query & Virtual), dnd-kit
- **Backend:** Play Framework (Scala), Slick, Caffeine, Akka Actors
- **Database**: PostgreSQL
- **Testing**: ScalaTest (backend)
- **CI**: Github Actions

## System Architecture
![System Architecture Diagram](/images/architecture.png)

## Entity Relationship Diagram (ERD)
![ERD](/images/erd.png)

## Code Coverage Report
Below is a screenshot of the backend test coverage report generated with sbt-scoverage:

![Code coverage report](/images/code-coverage.png)

## Project Structure
- `frontend/` – Frontend source code (UI).
- `backend/` – Backend API server.

## How to Run
Each part has its own detailed setup guide:
- [Frontend README](./frontend/README.md)
- [Backend README](./backend/README.md)

## Screenshots
Below are some captures showcasing key features of the application.

### Workspace & Boards
| Create Workspace | Create Board | Board Created |
|------------------|--------------|----------------|
| ![Create Workspace](images/screenshots/create-workspace.jpeg) | ![Create Board](images/screenshots/create-board.jpeg) | ![Board Created](images/screenshots/board-created.jpeg) |

### Tasks Management
| Create Task | Assign Member to Task | Move Task |
|--------------|----------------------|------------|
| ![Create Task](images/screenshots/create-task.jpeg) | ![Assign Member to Task](images/screenshots/assign-member-to-task.jpeg) | ![Move Task](images/screenshots/move-task.jpeg) |

### Board Operations
| Move Column | Default Board Detail |
|--------------|----------------------|
| ![Move Column](images/screenshots/move-column.jpeg) | ![Default Board Detail](images/screenshots/default-board-detail.jpeg) |

### Import / Export Project
| Import Project | Export Project |
|----------------|----------------|
| ![Import Project](images/screenshots/import-project.jpeg) | ![Export Project](images/screenshots/export-project.jpeg) |
