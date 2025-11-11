// User-related type definitions (Role-based system)

export interface User {
    id: string;
    name: string;
    email: string;
    avatar?: string;
    createdAt: string;
    updatedAt: string;
    // NO role field here - never trust frontend with roles
}

export type UserRole = 'admin' | 'user';

export interface AuthResponse {
    id: number;
    name: string;
    email: string;
}

export interface UserRoleResponse {
    role: UserRole;
}

// Route-related types
export interface RouteConfig {
    path: string;
    requiredRole?: UserRole;
    adminOnly?: boolean;
}

// Component prop types
export interface ProtectedRouteProps {
    children: React.ReactNode;
    requiredRole?: UserRole;
    adminOnly?: boolean;
    fallback?: React.ReactNode;
    redirectTo?: string;
}

// Layout props
export interface LayoutProps {
    children: React.ReactNode;
}

export interface UserProfile {
    userLanguage: string;
    themeMode: 'light' | 'dark';
}
