import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

const LoginForm: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [rememberMe, setRememberMe] = useState(false);
    const { login, isLoading, error, isAuthenticated, clearAuthError } =
        useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // Redirect if already authenticated
    useEffect(() => {
        if (isAuthenticated) {
            const from = (location.state as any)?.from || '/user/boards';
            navigate(from, { replace: true });
        }
    }, [isAuthenticated, navigate, location]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            await login(email, password);
            // Navigation will happen via useEffect above
        } catch (err) {
            // Error is handled by Redux state
            console.error('Login failed:', err);
        }
    };

    return (
        <div className='w-full max-w-md mx-auto'>
            {/* Logo/Brand */}
            <div className='text-center mb-8'>
                <div className='inline-flex items-center justify-center w-12 h-12 bg-blue-600 rounded-lg mb-4'>
                    <svg
                        className='w-8 h-8 text-white'
                        fill='currentColor'
                        viewBox='0 0 24 24'
                    >
                        <path d='M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5' />
                    </svg>
                </div>
                <h1 className='text-2xl font-semibold text-gray-900 mb-2'>
                    Sunny
                </h1>
                <p className='text-gray-600 font-bold text-lg'>
                    Log in to continue
                </p>
            </div>

            {/* Login Form */}
            <form onSubmit={handleSubmit} className='space-y-5'>
                {error && (
                    <div className='bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm'>
                        {error}
                        <button
                            type='button'
                            onClick={clearAuthError}
                            className='float-right text-red-400 hover:text-red-600 ml-2'
                        >
                            ×
                        </button>
                    </div>
                )}

                {/* Email Input */}
                <div>
                    <label
                        htmlFor='email'
                        className='block text-sm font-medium text-gray-700 mb-2'
                    >
                        Email <span className='text-red-500'>*</span>
                    </label>
                    <input
                        id='email'
                        type='email'
                        required
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        className='w-full px-3 py-3 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white'
                        placeholder='Enter your email'
                    />
                </div>

                {/* Password Input */}
                <div>
                    <label
                        htmlFor='password'
                        className='block text-sm font-medium text-gray-700 mb-2'
                    >
                        Password <span className='text-red-500'>*</span>
                    </label>
                    <input
                        id='password'
                        type='password'
                        required
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        className='w-full px-3 py-3 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white'
                        placeholder='Enter your password'
                    />
                </div>

                {/* Remember Me Checkbox */}
                <div className='flex items-center mt-4 mb-6'>
                    <input
                        id='remember-me'
                        type='checkbox'
                        checked={rememberMe}
                        onChange={e => setRememberMe(e.target.checked)}
                        className='h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded'
                    />
                    <label
                        htmlFor='remember-me'
                        className='ml-2 text-sm text-gray-700 flex items-center'
                    >
                        Remember me
                        <div className='ml-1 group relative'>
                            <svg
                                className='w-4 h-4 text-gray-400 cursor-help'
                                fill='currentColor'
                                viewBox='0 0 20 20'
                            >
                                <path
                                    fillRule='evenodd'
                                    d='M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z'
                                    clipRule='evenodd'
                                />
                            </svg>
                            <div className='invisible group-hover:visible absolute bottom-6 left-1/2 transform -translate-x-1/2 bg-gray-800 text-white text-xs rounded py-1 px-2 whitespace-nowrap z-10'>
                                Keep me logged in for 30 days
                            </div>
                        </div>
                    </label>
                </div>

                {/* Continue Button */}
                <button
                    type='submit'
                    disabled={isLoading}
                    className='w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-medium py-3 px-4 rounded-md transition-colors duration-200 disabled:cursor-not-allowed text-sm'
                >
                    {isLoading ? (
                        <div className='flex items-center justify-center'>
                            <div className='animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2'></div>
                            Signing in...
                        </div>
                    ) : (
                        'Continue'
                    )}
                </button>
            </form>

            {/* Footer Links */}
            <div className='mt-6 text-center space-y-2'>
                <div className='flex justify-center space-x-4 text-sm'>
                    <Link
                        to='/forgot-password'
                        className='text-blue-600 hover:text-blue-700 hover:underline'
                    >
                        Can't log in?
                    </Link>
                    <span className='text-gray-300'>•</span>
                    <Link
                        to='/signup'
                        className='text-blue-600 hover:text-blue-700 hover:underline'
                    >
                        Create an account
                    </Link>
                </div>

                {/* Atlassian Footer */}
                <div className='mt-8 pt-6 border-t border-gray-200'>
                    <div className='flex items-center justify-center mb-2'>
                        <svg
                            className='w-6 h-6 text-blue-600 mr-2'
                            fill='currentColor'
                            viewBox='0 0 24 24'
                        >
                            <path d='M7.5 2L2 7.5V22h14.5L22 16.5V2H7.5z' />
                        </svg>
                        <span className='text-sm font-medium text-gray-700'>
                            THOUSAND SUNNY
                        </span>
                    </div>
                    <p className='text-xs text-gray-500 mb-4'>
                        One account for Trello, Jira, Confluence and{' '}
                        <a href='#' className='text-blue-600 hover:underline'>
                            more
                        </a>
                    </p>
                    <div className='text-xs text-gray-400 space-y-1'>
                        <div className='flex justify-center space-x-4'>
                            <a href='#' className='hover:text-gray-600'>
                                Privacy Policy
                            </a>
                            <span>•</span>
                            <a href='#' className='hover:text-gray-600'>
                                User Notice
                            </a>
                        </div>
                        <p>
                            This site is protected by reCAPTCHA and the Google{' '}
                            <a href='#' className='hover:text-gray-600'>
                                Privacy Policy
                            </a>{' '}
                            and{' '}
                            <a href='#' className='hover:text-gray-600'>
                                Terms of Service
                            </a>{' '}
                            apply.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginForm;
