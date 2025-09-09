import React, { useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { Link } from 'react-router-dom';
import CreateWorkspaceModal from '@/components/shared/CreateModal';

interface ProfileDropdownProps {
    userName?: string;
    email?: string;
    handleLogout: () => void;
    handleCreateWorkspace: () => void;
}

const ProfileDropDown: React.FC<ProfileDropdownProps> = ({
    userName,
    email,
    handleLogout,
    handleCreateWorkspace,
}) => {

    return (
        <div className='absolute right-0 mt-2 w-80 bg-[#1E2125] rounded-lg shadow-lg shadow-[0px_8px_12px_#091e4226,0px_0px_1px_#091e424f] border border-gray-600 z-50'>
            <div className='p-4'>
                <div className='text-xs text-[#B6C2CF] uppercase tracking-wide font-semibold mb-3'>
                    ACCOUNT
                </div>

                <div className='flex items-center space-x-3 mb-4'>
                    <div className='w-10 h-10 bg-gradient-to-br from-yellow-400 to-orange-500 rounded-full flex items-center justify-center text-white font-semibold'>
                        {userName
                            ?.charAt(0)
                            .toUpperCase() || 'VT'}
                    </div>
                    <div>
                        <div className='font-medium text-[#B6C2CF]'>
                            {userName || 'Vu Tran'}
                        </div>
                        <div className='text-sm text-[#B6C2CF]'>
                            {email ||
                                'tranmster5000@gmail.com'}
                        </div>
                    </div>
                </div>

                <div className='border-t border-gray-600 pt-4'>
                    {/* <div className='space-y-1 mb-4'>
                        <button className='w-full text-left px-3 py-2 text-sm text-[#B6C2CF] hover:bg-gray-700 rounded'>
                            Profile and visibility
                        </button>
                        <button className='w-full text-left px-3 py-2 text-sm text-[#B6C2CF] hover:bg-gray-700 rounded'>
                            Activity
                        </button>
                        <button className='w-full text-left px-3 py-2 text-sm text-[#B6C2CF] hover:bg-gray-700 rounded'>
                            Cards
                        </button>
                        <button className='w-full text-left px-3 py-2 text-sm text-[#B6C2CF] hover:bg-gray-700 rounded'>
                            Settings
                        </button>
                        <button className='w-full text-left px-3 py-2 text-sm text-[#B6C2CF] hover:bg-gray-700 rounded flex items-center justify-between'>
                            Theme
                            <svg
                                className='w-4 h-4'
                                fill='none'
                                viewBox='0 0 24 24'
                                stroke='currentColor'
                            >
                                <path
                                    strokeLinecap='round'
                                    strokeLinejoin='round'
                                    strokeWidth={2}
                                    d='M9 5l7 7-7 7'
                                />
                            </svg>
                        </button>
                    </div> */}

                    <button
                        onClick={handleCreateWorkspace} 
                        className='w-full text-left border-t border-gray-600 px-3 py-2 text-sm text-blue-600 hover:bg-blue-50 rounded flex items-center'>
                        <svg
                            className='w-4 h-4 mr-2'
                            fill='none'
                            viewBox='0 0 24 24'
                            stroke='currentColor'
                        >
                            <path
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeWidth={2}
                                d='M12 6v6m0 0v6m0-6h6m-6 0H6'
                            />
                        </svg>
                        Create Workspace
                    </button>
                </div>

                <div className='border-t border-gray-600 pt-4'>
                    <button
                        onClick={handleLogout}
                        className='w-full text-left px-3 py-2 text-sm text-[#B6C2CF] hover:bg-gray-700 rounded'
                    >
                        Log out
                    </button>
                </div>
            </div>
        </div>
    )
}

const Navbar: React.FC = () => {
    const { user, logout } = useAuth();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isProfileOpen, setIsProfileOpen] = useState(false);

    const handleCreateWorkspace = () => {
        setIsModalOpen(true);
        setIsProfileOpen(false);
    };

    const handleLogout = async () => {
        await logout();
    };

    return (
        <nav className='bg-[#1E2125] border-b border-gray-700 px-4 py-2'>
            <div className='flex items-center justify-between'>
                {/* Left side - Logo and Navigation */}
                <div className='flex items-center space-x-4'>
                    {/* Grid Icon */}
                    <button className='p-2 text-gray-300 hover:text-white hover:bg-gray-700 rounded'>
                        <svg
                            className='w-4 h-4'
                            fill='currentColor'
                            viewBox='0 0 24 24'
                        >
                            <path d='M4 4h4v4H4V4zm6 0h4v4h-4V4zm6 0h4v4h-4V4zM4 10h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4zM4 16h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4z' />
                        </svg>
                    </button>

                    {/* Trello Logo */}
                    <Link to={'/'}>
                        <div className='flex items-center space-x-2'>
                            <div className='bg-blue-600 p-1 rounded'>
                                <svg
                                    className='w-5 h-5 text-white'
                                    fill='currentColor'
                                    viewBox='0 0 24 24'
                                >
                                    <path d='M21 16.5c0 .38-.21.71-.53.88l-7.9 4.44c-.16.12-.36.18-.57.18-.21 0-.41-.06-.57-.18l-7.9-4.44A.991.991 0 0 1 3 16.5v-9c0-.38.21-.71.53-.88l7.9-4.44c.16-.12.36-.18.57-.18.21 0 .41.06.57.18l7.9 4.44c.32.17.53.5.53.88v9z' />
                                </svg>
                            </div>
                            <span className='text-white font-semibold text-lg'>
                                Smart Taskhub
                            </span>
                        </div>
                    </Link>
                </div>

                {/* Center - Search */}
                <div className='flex-1 max-w-lg mx-4'>
                    <div className='relative'>
                        <div className='absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none'>
                            <svg
                                className='h-4 w-4 text-gray-400'
                                fill='none'
                                viewBox='0 0 24 24'
                                stroke='currentColor'
                            >
                                <path
                                    strokeLinecap='round'
                                    strokeLinejoin='round'
                                    strokeWidth={2}
                                    d='M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z'
                                />
                            </svg>
                        </div>
                        <input
                            type='text'
                            placeholder='Search'
                            className='block w-full pl-10 pr-3 py-1.5 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm'
                        />
                    </div>
                </div>

                {/* Right side - Actions and Profile */}
                <div className='flex items-center space-x-2'>
                    {/* Create Button */}
                    <button className='bg-blue-600 hover:bg-blue-700 text-white px-3 py-1.5 rounded text-sm font-medium'>
                        Create
                    </button>

                    {/* Notifications */}
                    {/* <button className='p-1.5 text-gray-300 hover:text-white hover:bg-gray-700 rounded relative'>
                        <svg
                            className='w-5 h-5'
                            fill='none'
                            viewBox='0 0 24 24'
                            stroke='currentColor'
                        >
                            <path
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeWidth={2}
                                d='M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9'
                            />
                        </svg>
                        <span className='absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center'>
                            1
                        </span>
                    </button> */}

                    {/* Help */}
                    {/* <button className='p-1.5 text-gray-300 hover:text-white hover:bg-gray-700 rounded'>
                        <svg
                            className='w-5 h-5'
                            fill='none'
                            viewBox='0 0 24 24'
                            stroke='currentColor'
                        >
                            <path
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeWidth={2}
                                d='M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z'
                            />
                        </svg>
                    </button> */}

                    {/* Profile Dropdown */}
                    <div className='relative'>
                        <button
                            onClick={() => setIsProfileOpen(!isProfileOpen)}
                            className='flex items-center space-x-1 p-1 hover:bg-gray-700 rounded'
                        >
                            <div className='w-8 h-8 bg-gradient-to-br from-yellow-400 to-orange-500 rounded-full flex items-center justify-center text-white font-semibold text-sm'>
                                {user?.name?.charAt(0).toUpperCase() || 'VT'}
                            </div>
                        </button>

                        {/* Profile Dropdown Menu */}
                        {isProfileOpen && (
                            <ProfileDropDown
                                userName={user?.name}
                                email={user?.email}
                                handleCreateWorkspace={handleCreateWorkspace}
                                handleLogout={handleLogout}
                            />
                        )}
                    </div>
                </div>
            </div>

            {/* Close dropdown when clicking outside */}
            {isProfileOpen && (
                <div
                    className='fixed inset-0 z-40'
                    onClick={() => setIsProfileOpen(false)}
                />
            )}

            {/* Create Workspace Modal */}
            <CreateWorkspaceModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
            />
        </nav>
    );
};

export default Navbar;
