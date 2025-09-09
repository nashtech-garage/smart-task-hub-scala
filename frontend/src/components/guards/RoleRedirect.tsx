import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

export const RoleRedirect: React.FC = () => {
    const { isAuthenticated, userRole, isLoading, isInitialized, user } =
        useAuth();

    // Redirect to login if not authenticated
    if (!isAuthenticated) {
        return <Navigate to='/login' replace />;
    }

    // Show loading while fetching user role
    if (isLoading || !isInitialized) {
        return (
            <div className='min-h-screen flex items-center justify-center'>
                <div className='text-center'>
                    <div className='animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4'></div>
                    <p className='text-gray-600'>
                        Setting up your workspace...
                    </p>
                </div>
            </div>
        );
    }

    // Redirect based on user role
    if (userRole === 'admin') {
        return <Navigate to='/admin/dashboard' replace />;
    }

    if (userRole === 'user') {
        return <Navigate to='/user/boards' replace />;
    }

    // Fallback if role is not recognized
    return (
        <div className='min-h-screen flex items-center justify-center'>
            <div className='text-center'>
                <h2 className='text-2xl font-semibold text-gray-900 mb-2'>
                    Welcome, {user?.name}!
                </h2>
                <p className='text-gray-600 mb-4'>
                    Your account setup is incomplete. Please contact an
                    administrator.
                </p>
                <Navigate to='/login' replace />
            </div>
        </div>
    );
};

