import {
    createBrowserRouter,
    RouterProvider,
    Navigate,
    Outlet,
} from 'react-router-dom';
import ProtectedRoute from '@/components/guards/ProtectedRoute';
import { MainLayout } from '@/components/layout/MainLayout';

// Shared pages
import NotFound from '@/components/shared/NotFound';
import UnauthorizedFallback from '@/components/shared/UnauthorizedFallback';

// Auth pages
import LoginForm from '@/components/auth/LoginForm';
import AuthLayout from '@/components/layout/AuthLayout';
import RegisterForm from '@/components/auth/RegisterForm';
import SidebarLayout from '@/components/layout/SidebarLayout';
import Template from '@/pages/Template';
import Home from '@/pages/Home';
import Member from '@/pages/workspace/Member';
import Setting from '@/pages/workspace/Setting';
import WorkspaceBoard from '@/pages/WorkspaceBoard';
import HomeBoards from '@/pages/HomeBoards';
import Boards from '@/pages/workspace/Board';
import WorkspaceDetail from '@/pages/workspace/WorkspaceDetail';

// Layout wrappers for different roles
const AdminLayout = () => (
    <ProtectedRoute adminOnly fallback={<UnauthorizedFallback />}>
        <MainLayout>
            <Outlet />
        </MainLayout>
    </ProtectedRoute>
);

const UserLayout = () => (
    <ProtectedRoute requiredRole='user' fallback={<UnauthorizedFallback />}>
        <MainLayout>
            <Outlet />
        </MainLayout>
    </ProtectedRoute>
);

const router = createBrowserRouter([
    // Public routes
    {
        path: '/',
        element: <AuthLayout />,
        children: [
            {
                index: true,
                element: <Navigate to={'/login'} replace />,
            },
            {
                path: 'login',
                element: <LoginForm />,
            },
            {
                path: 'signup',
                element: <RegisterForm />,
            },
        ],
    },

    // Admin routes - only accessible by ADMIN role
    {
        path: '/admin',
        element: <AdminLayout />,
        children: [
            {
                index: true,
                element: <Navigate to='/admin/dashboard' replace />,
            },
        ],
    },

    // User routes - only accessible by USER role
    {
        path: '/',
        element: <UserLayout />,
        children: [
            {
                path: 'user',
                element: <SidebarLayout />,
                children: [
                    {
                        path: 'boards',
                        element: <HomeBoards />,
                    },
                    {
                        path: 'templates',
                        element: <Template />
                    },
                    {
                        path: 'home',
                        element: <Home />
                    },
                    {
                        path: 'detail',
                        element: <WorkspaceDetail />,
                    }
                ]
            },
            {
                path: 'board/:boardId',
                element: <WorkspaceBoard />,
            },
            {
                path: 'workspace',
                element: <SidebarLayout />,
                children: [
                    {
                        path: 'boards/:id',
                        element: <Boards />,
                    },
                    {
                        path: 'members/:id',
                        element: <Member />,
                    },
                    {
                        path: 'setting/:id',
                        element: <Setting />,
                    },

                ]
            },
        ],
    },

    // Error routes
    {
        path: '/unauthorized',
        element: <UnauthorizedFallback />,
    },
    {
        path: '/not-found',
        element: <NotFound />,
    },
    {
        path: '*',
        element: <NotFound />,
    },
]);

export const AppRouter = () => {
    return <RouterProvider router={router} />;
};
