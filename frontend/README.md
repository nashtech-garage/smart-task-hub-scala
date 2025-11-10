# Smart Taskhub - UI

This is the **frontend application** for Smart Taskhub, built with **React** and **TypeScript**.

---

## Tech Stack

- **Frontend:** React **18.2.0** + TypeScript **5.8.3**
- **Build Tool:** Vite **7.0.6**
- **Styling:** Tailwind CSS **4.1.11**
- **State Management:** Redux Toolkit **2.8.2**, Redux Persist **6.0.0**
- **Data Fetching & Virtualization:** TanStack React Query **5.90.3**, React Virtual **3.13.12**
- **Drag & Drop:** dnd-kit (**@dnd-kit/core 6.3.1**, **@dnd-kit/sortable 10.0.0**)
- **Routing:** React Router DOM **7.7.0**
- **UI Icons:** Lucide React **0.536.0**
- **Testing:** Vitest **3.2.4**, Testing Library React **14.2.1**
- **Linting & Formatting:** ESLint **9.30.1**, Prettier **3.6.2**

---

## Setup Instructions
### 1. Install Dependencies
```bash
npm install
```

### 2. Start Development Server
```bash
npm run dev
```

### 3. Build for Production
```bash
npm run build
```

### 4. Lint & Type Check
```bash
npm run lint
npm run format:check
npx tsc --noEmit
```

## Environment Variables
By default, the frontend connects to the API at:
```bash
http://localhost:9000/api
```
If your backend runs on a different URL, create a `.env` file in the root directory of the frontend and specify your API base URL as follows:
```bash
VITE_API_URL=YOUR_API_URL
```
Example:
```bash
VITE_API_URL=https://myserver.com/api
```
## 📂 Project Structure
```bash
frontend/
│
├── public/ # Static assets
└── src/
    ├── assets/ # Images, icons, and static files
    ├── components/ # Reusable UI components
    ├── hooks/ # Custom React hooks
    ├── lib/ # Utility libraries and configs
    ├── pages/ # Page components
    ├── router/ # Routing configuration
    ├── services/ # API services
    ├── store/ # State management (Redux)
    ├── test/ # Unit and integration tests
    ├── types/ # TypeScript types and interfaces
    ├── websocket/ # WebSocket event handlers for real-time updates
    └── utils/ # Utility/helper functions
```