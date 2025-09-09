# Smart Taskhub - UI

This is the **frontend application** for Smart Taskhub, built with **React** and **TypeScript**.

---

## ⚙️ Tech Stack
- **React + TypeScript**
- **Vite** for build
- **TailwindCSS** for styling

---

## 🛠 Setup Instructions
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

## ⚙️ Environment Variables
Update .env file with the  in the root of the frontend folder:
```bash
VITE_API_URL=http://localhost:9000/api
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
└── utils/ # Utility/helper functions

```