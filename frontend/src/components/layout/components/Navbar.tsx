import React, { useContext, useEffect, useRef, useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { Link, useNavigate } from 'react-router-dom';
import CreateWorkspaceModal from '@/components/shared/CreateModal';
import { useDebounce } from "use-debounce";
import taskService from '@/services/taskService';
import type { TaskSearchResponse } from '@/types';
import { StickyNote } from 'lucide-react';
import { SearchContext } from '../MainLayout';
import ProfileDropDown from './ProfileDropDown';

const Navbar: React.FC = () => {
    const { user, logout } = useAuth();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isProfileOpen, setIsProfileOpen] = useState(false);
    const [keyword, setKeyword] = useState('');
    const [debounceQuery] = useDebounce(keyword, 500);
    const [isLoading, setIsLoading] = useState(false);
    const [results, setResults] = useState<TaskSearchResponse[]>([]);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const searchRef = useRef<HTMLDivElement>(null);
    const navigate = useNavigate();
    const searchContext = useContext(SearchContext);

    if (!searchContext) return null;
    const { showSearch } = searchContext;

    const handleCreateWorkspace = () => {
        setIsModalOpen(true);
        setIsProfileOpen(false);
    };

    const handleLogout = async () => {
        await logout();
    };

    const handleSearch = async (keyword: string) => {
        try {
            if (!keyword.trim()) {
                setResults([]);
                return;
            }
            setIsLoading(true);
            const res = await taskService.searchTasks(keyword.trim());
            setResults(res.data || []);
            setIsDropdownOpen(true);
        } catch (error) {
            console.error('Search error:', error);
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (
                searchRef.current &&
                !searchRef.current.contains(event.target as Node)
            ) {
                setIsDropdownOpen(false);
                setKeyword('');
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    useEffect(() => {
        if (debounceQuery) {
            handleSearch(debounceQuery);
        } else {
            setResults([]);
            setIsDropdownOpen(false);
        }
    }, [debounceQuery]);

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
                {showSearch &&
                    <div className='flex-1 max-w-xl mx-4' ref={searchRef}>
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
                                value={keyword}
                                onChange={(e) => setKeyword(e.target.value)}
                                className='block w-full pl-10 pr-3 py-1.5 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm'
                            />
                        </div>

                        {/* Dropdown results */}
                        {isDropdownOpen && (
                            <div className="absolute mt-1 w-full max-w-xl bg-[#1E2125] border border-gray-700 rounded-md shadow-lg z-50">
                                {/* Scrollable list */}
                                <div className="max-h-[50%] overflow-y-auto">
                                    {isLoading ? (
                                        <div className="p-2 text-gray-400 text-sm">Loading...</div>
                                    ) : results.length > 0 ? (
                                        results.map((task) => (
                                            <div
                                                key={task.taskId}
                                                className="flex items-start px-3 py-2 cursor-pointer hover:bg-gray-700 mb-1"
                                                onClick={() => {
                                                    setIsDropdownOpen(false);
                                                    setKeyword('');
                                                    navigate(`/board/${task.projectId}`);
                                                }}
                                            >
                                                {/* Icon bên trái */}
                                                <div className="flex-shrink-0 mt-0.5 text-gray-400">
                                                    <StickyNote />
                                                </div>

                                                {/* Nội dung task */}
                                                <div className="ml-2">
                                                    <div className="text-white text-sm font-medium">
                                                        {task.taskName}
                                                    </div>
                                                    <div className="text-gray-400 text-xs">
                                                        {task.projectName}: {task.columnName} {task.taskStatus === 'archived' && `• ${task.taskStatus}`}
                                                    </div>
                                                </div>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="p-2 text-gray-400 text-sm">No results</div>
                                    )}
                                </div>

                                {/* Advanced Search link */}
                                <div
                                    className="border-t border-gray-700 px-3 py-2 text-blue-400 text-sm cursor-pointer hover:bg-gray-800"
                                    onClick={() => {
                                        setIsDropdownOpen(false);
                                        setKeyword('');
                                        navigate(`/search`);
                                    }}
                                >
                                    Advanced Search
                                </div>
                            </div>
                        )}

                    </div>
                }



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
