import type { ApiResponse } from '@/types';
import type { AuthResponse, UserRole } from '@/types/user.types';
import axiosClients from './axiosClient';

const baseURL = '/auth';

const authService = {
    // Register user
    register(
        name: string,
        email: string,
        password: string
    ): Promise<ApiResponse<AuthResponse>> {
        return axiosClients.post(`${baseURL}/register`, {
            name,
            email,
            password,
        });
    },

    // Login user
    login(email: string, password: string): Promise<ApiResponse<AuthResponse>> {
        return axiosClients.post(`${baseURL}/login`, {
            email,
            password,
        });
    },

    // Check if user is authenticated (validate cookie)
    getMe(): Promise<ApiResponse<AuthResponse>> {
        return axiosClients.get(`${baseURL}/me`);
    },

    // Get user role (backend determines based on database)
    async getUserRole(): Promise<ApiResponse<UserRole>> {
        return axiosClients.get(`${baseURL}/role`);
    },

    // Logout user
    async logout(): Promise<void> {
        axiosClients.post(`${baseURL}/logout`);
    },

    // Forgot password
    async forgotPassword(email: string): Promise<{ message: string }> {
        const response = await fetch(`${baseURL}/forgot-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email }),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to send reset email');
        }

        return response.json();
    },

    // Reset password
    async resetPassword(
        token: string,
        newPassword: string
    ): Promise<{ message: string }> {
        const response = await fetch(`${baseURL}/reset-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ token, newPassword }),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to reset password');
        }

        return response.json();
    },
};

export default authService;
