import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import type { ProtectedRouteProps } from '@/types/user.types';
import LoadingSpinner from '../ui/LoadingSpinner';
import UnauthorizedFallback from '../shared/UnauthorizedFallback';

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
    children,
    requiredRole,
    adminOnly = false,
    fallback,
    redirectTo,
}) => {
    const { isAuthenticated, isLoading, userRole, isInitialized } = useAuth();
    const location = useLocation();

    // Show loading while checking authentication
    if (isLoading || !isInitialized) {
        return <LoadingSpinner />;
    }

    // Redirect to login if not authenticated
    if (!isAuthenticated) {
        return (
            <Navigate to='/login' state={{ from: location.pathname }} replace />
        );
    }

    // Check admin-only access
    if (adminOnly && userRole !== 'admin') {
        if (redirectTo) {
            return <Navigate to={redirectTo} replace />;
        }
        return fallback || <UnauthorizedFallback />;
    }

    // Check specific role requirement
    if (requiredRole && userRole !== requiredRole) {
        if (redirectTo) {
            return <Navigate to={redirectTo} replace />;
        }
        return fallback || <UnauthorizedFallback />;
    }

    // All checks passed - render children
    return <>{children}</>;
};

export default ProtectedRoute;

