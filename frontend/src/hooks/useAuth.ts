import { useSelector, useDispatch } from 'react-redux';
import { useCallback, useEffect } from 'react';
import type { RootState, AppDispatch } from '@/store';
import {
    loginUser,
    logoutUser,
    checkAuthStatus,
    clearError,
} from '@/store/slices/authSlice';
import type { UserRole } from '@/types/user.types';

export const useAuth = () => {
    const dispatch = useDispatch<AppDispatch>();
    const authState = useSelector((state: RootState) => state.auth);

    // Login function
    const login = useCallback(
        async (email: string, password: string) => {
            const result = await dispatch(loginUser({ email, password }));
            if (loginUser.fulfilled.match(result)) {
                return result.payload;
            } else {
                console.error(result);
                throw new Error(result.payload as string);
            }
        },
        [dispatch]
    );

    // Logout function
    const logout = useCallback(async () => {
        await dispatch(logoutUser());
    }, [dispatch]);

    // Check authentication status
    const checkAuth = useCallback(async () => {
        return dispatch(checkAuthStatus());
    }, [dispatch]);

    // Clear auth error
    const clearAuthError = useCallback(() => {
        dispatch(clearError());
    }, [dispatch]);

    // Role checking functions
    const hasRole = useCallback(
        (role: UserRole): boolean => {
            return authState.userRole === role;
        },
        [authState.userRole]
    );

    const isAdmin = useCallback((): boolean => {
        return authState.userRole === 'admin';
    }, [authState.userRole]);

    const isUser = useCallback((): boolean => {
        return authState.userRole === 'user';
    }, [authState.userRole]);

    // Initialize auth check on app start
    useEffect(() => {
        if (!authState.isInitialized && !authState.isLoading) {
            checkAuth();
        }
    }, [authState.isInitialized, authState.isLoading, checkAuth]);

    return {
        // State
        user: authState.user,
        userRole: authState.userRole,
        // userRole: 'user',
        isAuthenticated: authState.isAuthenticated,
        // isAuthenticated: true,
        isLoading: authState.isLoading,
        error: authState.error,
        isInitialized: authState.isInitialized,

        // Actions
        login,
        logout,
        checkAuth,
        clearAuthError,

        // Role checkers
        hasRole,
        isAdmin,
        isUser,
    };
};
