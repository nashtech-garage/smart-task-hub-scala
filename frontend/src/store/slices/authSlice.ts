import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import authService from '@/services/authService';
import type { AuthResponse, UserRole } from '@/types/user.types';

interface AuthState {
    user: AuthResponse | null;
    userRole: UserRole | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    error: string | null;
    isInitialized: boolean; // Track if we've checked auth status on app start
}

const initialState: AuthState = {
    user: null,
    userRole: null,
    isAuthenticated: false,
    isLoading: false,
    error: null,
    isInitialized: false,
};

// Login user
export const loginUser = createAsyncThunk(
    'auth/login',
    async (
        { email, password }: { email: string; password: string },
        { rejectWithValue }
    ) => {
        try {
            const response = await authService.login(email, password);
            // After successful login, fetch user role
            const roleResponse = await authService.getUserRole();
            return {
                user: response.data,
                role: roleResponse.data,
            };
        } catch (error: any) {
            return rejectWithValue(error.message || 'Login failed');
        }
    }
);

// Check authentication status
export const checkAuthStatus = createAsyncThunk(
    'auth/checkStatus',
    async (_, { rejectWithValue }) => {
        try {
            const response = await authService.getMe();
            const roleResponse = await authService.getUserRole();

            return {
                user: response.data,
                role: roleResponse.data,
            };
        } catch (error: any) {
            return rejectWithValue(
                error.message || 'Authentication check failed'
            );
        }
    }
);

// Logout user
export const logoutUser = createAsyncThunk(
    'auth/logout',
    async (_, { rejectWithValue }) => {
        try {
            await authService.logout();
            return null;
        } catch (error: any) {
            return rejectWithValue(error.message || 'Logout failed');
        }
    }
);

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        // Synchronous actions
        clearError: state => {
            state.error = null;
        },
        setInitialized: state => {
            state.isInitialized = true;
        },
        // Manual logout (for cases like token expiration)
        forceLogout: state => {
            state.user = null;
            state.userRole = null;
            state.isAuthenticated = false;
            state.error = 'Session expired';
        },
    },
    extraReducers: builder => {
        // Login
        builder
            .addCase(loginUser.pending, state => {
                state.isLoading = true;
                state.error = null;
            })
            .addCase(
                loginUser.fulfilled,
                (
                    state,
                    action: PayloadAction<{
                        user: AuthResponse;
                        role: UserRole;
                    }>
                ) => {
                    state.isLoading = false;
                    state.user = action.payload.user;
                    state.userRole = action.payload.role;
                    state.isAuthenticated = true;
                    state.error = null;
                    state.isInitialized = true;
                }
            )
            .addCase(loginUser.rejected, state => {
                state.isLoading = false;
                state.user = null;
                state.userRole = null;
                state.isAuthenticated = false;
                // state.error = action.payload as string;
                state.error = 'Invalid email or password!' as string;
                state.isInitialized = true;
            });

        // Check auth status
        builder
            .addCase(checkAuthStatus.pending, state => {
                state.isLoading = true;
            })
            .addCase(
                checkAuthStatus.fulfilled,
                (
                    state,
                    action: PayloadAction<{
                        user: AuthResponse;
                        role: UserRole;
                    }>
                ) => {
                    state.isLoading = false;
                    state.user = action.payload.user;
                    state.userRole = action.payload.role;
                    state.isAuthenticated = true;
                    state.error = null;
                    state.isInitialized = true;
                }
            )
            .addCase(checkAuthStatus.rejected, state => {
                state.isLoading = false;
                state.user = null;
                state.userRole = null;
                state.isAuthenticated = false;
                state.error = null;
                state.isInitialized = true;
            });

        // Logout
        builder
            .addCase(logoutUser.pending, state => {
                state.isLoading = true;
            })
            .addCase(logoutUser.fulfilled, state => {
                state.isLoading = false;
                state.user = null;
                state.userRole = null;
                state.isAuthenticated = false;
                state.error = null;
            })
            .addCase(logoutUser.rejected, state => {
                state.isLoading = false;
                // Even if logout API fails, clear local state
                state.user = null;
                state.userRole = null;
                state.isAuthenticated = false;
                state.error = null;
            });
    },
});

export const { clearError, setInitialized, forceLogout } = authSlice.actions;
export default authSlice.reducer;
