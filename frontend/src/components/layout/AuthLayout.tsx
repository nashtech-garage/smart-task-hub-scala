import React from 'react';
import { Outlet } from 'react-router-dom';

const AuthLayout: React.FC = () => {
    return (
        <div className='min-h-screen w-full bg-gradient-to-br from-blue-50 via-white to-blue-50 relative overflow-hidden'>
            {/* Background decorative elements */}
            <div className='absolute inset-0 pointer-events-none'>
                {/* Geometric shapes in background */}
                <div className='absolute top-20 left-10 w-32 h-32 bg-blue-100 rounded-full opacity-20 blur-xl'></div>
                <div className='absolute top-40 right-20 w-24 h-24 bg-indigo-100 rounded-full opacity-30 blur-lg'></div>
                <div className='absolute bottom-40 left-1/4 w-40 h-40 bg-sky-100 rounded-full opacity-15 blur-2xl'></div>
            </div>

            {/* Main content container */}
            <div className='relative z-10 min-h-screen flex items-center justify-center px-4 py-8'>
                {/* Left side - Decorative image */}
                <div className='hidden lg:block absolute bottom-0 left-0 w-80 h-80 pointer-events-none'>
                    <img
                        src='/src/assets/images/auth-layout/auth-bg-left.svg'
                        alt=''
                        className='w-full h-full object-contain object-bottom opacity-90'
                        onError={e => {
                            const target = e.target as HTMLImageElement;
                            target.style.display = 'none';
                        }}
                    />
                </div>

                {/* Center - Auth form */}
                <div className='w-full max-w-sm mx-auto bg-white/80 backdrop-blur-sm rounded-lg p-1 shadow-sm'>
                    <div className='bg-white rounded-lg p-8'>
                        <Outlet />
                    </div>
                </div>

                {/* Right side - Decorative image */}
                <div className='hidden lg:block absolute bottom-0 right-0 w-80 h-80 pointer-events-none'>
                    <img
                        src='/src/assets/images/auth-layout/auth-bg-right.svg'
                        alt=''
                        className='w-full h-full object-contain object-bottom opacity-90'
                        onError={e => {
                            const target = e.target as HTMLImageElement;
                            target.style.display = 'none';
                        }}
                    />
                </div>
            </div>

            {/* Mobile decorative elements */}
            <div className='lg:hidden absolute bottom-4 left-0 right-0 flex justify-between items-end px-4 pointer-events-none'>
                <img
                    src='/src/assets/images/auth-layout/auth-bg-left.svg'
                    alt=''
                    className='w-16 h-16 object-contain opacity-60'
                    onError={e => {
                        const target = e.target as HTMLImageElement;
                        target.style.display = 'none';
                    }}
                />
                <img
                    src='/src/assets/images/auth-layout/auth-bg-right.svg'
                    alt=''
                    className='w-16 h-16 object-contain opacity-60'
                    onError={e => {
                        const target = e.target as HTMLImageElement;
                        target.style.display = 'none';
                    }}
                />
            </div>
        </div>
    );
};

export default AuthLayout;
