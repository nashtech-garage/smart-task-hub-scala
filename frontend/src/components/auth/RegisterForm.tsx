import authService from '@/services/authService';
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const RegisterForm: React.FC = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
    });
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        setIsLoading(true);
        try {
            // console.log('Registration data:', formData);
            await authService.register(
                formData.name,
                formData.email,
                formData.password
            );
            navigate('/login');
        } catch (err) {
            console.log(err);
            setError('Registration failed. Please try again.');
        } finally {
            setIsLoading(false);
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
                    Create your account
                </p>
            </div>

            {/* Registration Form */}
            <form onSubmit={handleSubmit} className='space-y-4'>
                {error && (
                    <div className='bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm'>
                        {error}
                        <button
                            type='button'
                            onClick={() => setError('')}
                            className='float-right text-red-400 hover:text-red-600 ml-2'
                        >
                            ×
                        </button>
                    </div>
                )}

                {/* Name Input */}
                <div>
                    <label
                        htmlFor='name'
                        className='block text-sm font-medium text-gray-700 mb-1'
                    >
                        Full Name <span className='text-red-500'>*</span>
                    </label>
                    <input
                        id='name'
                        name='name'
                        type='text'
                        required
                        value={formData.name}
                        onChange={handleChange}
                        className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors'
                        placeholder='Enter your full name'
                    />
                </div>

                {/* Email Input */}
                <div>
                    <label
                        htmlFor='email'
                        className='block text-sm font-medium text-gray-700 mb-1'
                    >
                        Email <span className='text-red-500'>*</span>
                    </label>
                    <input
                        id='email'
                        name='email'
                        type='email'
                        required
                        value={formData.email}
                        onChange={handleChange}
                        className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors'
                        placeholder='Enter your email'
                    />
                </div>

                {/* Password Input */}
                <div>
                    <label
                        htmlFor='password'
                        className='block text-sm font-medium text-gray-700 mb-1'
                    >
                        Password <span className='text-red-500'>*</span>
                    </label>
                    <input
                        id='password'
                        name='password'
                        type='password'
                        required
                        minLength={6}
                        value={formData.password}
                        onChange={handleChange}
                        className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors'
                        placeholder='Create a password'
                    />
                </div>

                {/* Confirm Password Input */}
                <div>
                    <label
                        htmlFor='confirmPassword'
                        className='block text-sm font-medium text-gray-700 mb-1'
                    >
                        Confirm Password <span className='text-red-500'>*</span>
                    </label>
                    <input
                        id='confirmPassword'
                        name='confirmPassword'
                        type='password'
                        required
                        minLength={6}
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors'
                        placeholder='Confirm your password'
                    />
                </div>

                {/* Create Account Button */}
                <button
                    type='submit'
                    disabled={isLoading}
                    className='w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-medium py-2.5 px-4 rounded-lg transition-colors duration-200 disabled:cursor-not-allowed'
                >
                    {isLoading ? (
                        <div className='flex items-center justify-center'>
                            <div className='animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2'></div>
                            Creating account...
                        </div>
                    ) : (
                        'Create Account'
                    )}
                </button>
            </form>

            {/* Footer Links */}
            <div className='mt-6 text-center'>
                <p className='text-sm text-gray-600'>
                    Already have an account?{' '}
                    <Link
                        to='/login'
                        className='text-blue-600 hover:text-blue-700 hover:underline font-medium'
                    >
                        Sign in
                    </Link>
                </p>

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
                        <p>
                            By creating an account, you agree to our{' '}
                            <a href='#' className='hover:text-gray-600'>
                                Terms of Service
                            </a>{' '}
                            and{' '}
                            <a href='#' className='hover:text-gray-600'>
                                Privacy Policy
                            </a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RegisterForm;
