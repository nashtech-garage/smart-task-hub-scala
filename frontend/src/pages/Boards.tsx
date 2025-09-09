import { useState } from "react";

interface Template {
    id: string;
    title: string;
    category: string;
    image: string;
    color: string;
}

interface Board {
    id: string;
    title: string;
    color: string;
}

const templates: Template[] = [
    {
        id: '1',
        title: 'Basic Board',
        category: 'project-management',
        image: '/api/placeholder/300/200',
        color: 'bg-blue-500',
    },
    {
        id: '2',
        title: 'Kanban Template',
        category: 'project-management',
        image: '/api/placeholder/300/200',
        color: 'bg-gradient-to-br from-pink-400 to-blue-400',
    },
    {
        id: '3',
        title: 'Daily Task Management Template | Trello',
        category: 'productivity',
        image: '/api/placeholder/300/200',
        color: 'bg-gradient-to-br from-gray-600 to-gray-800',
    },
    {
        id: '4',
        title: 'Remote Team Hub',
        category: 'remote-work',
        image: '/api/placeholder/300/200',
        color: 'bg-gradient-to-br from-yellow-400 to-orange-500',
    },
];

const recentBoards: Board[] = [
    {
        id: '1',
        title: 'Daily Task Management Template | Trello',
        color: 'bg-gradient-to-br from-gray-600 to-gray-800',
    },
    {
        id: '2',
        title: 'Smart TaskHub',
        color: 'bg-blue-600',
    },
];

const workspaceBoards: Board[] = [
    {
        id: '1',
        title: 'Smart TaskHub',
        color: 'bg-blue-600',
    },
];

const Boards = () => {
    const [selectedCategory, setSelectedCategory] = useState('all');
    
        return (
            <div className='p-6'>
                {/* Most Popular Templates */}
                <div className='mb-8'>
                    <div className='flex items-center justify-between mb-4'>
                        <h2 className='text-xl font-bold text-white'>
                            Most popular templates
                        </h2>
                        <button className='text-sm text-gray-500 hover:text-gray-700'>
                            ×
                        </button>
                    </div>
                    <p className='text-sm text-white mb-4'>
                        Get going faster with a template from the Trello
                        community or{' '}
                        <select
                            className='border border-gray-300 rounded px-2 py-1 text-sm'
                            value={selectedCategory}
                            onChange={e =>
                                setSelectedCategory(e.target.value)
                            }
                        >
                            <option value='all'>choose a category</option>
                            <option value='project-management'>
                                Project Management
                            </option>
                            <option value='productivity'>
                                Productivity
                            </option>
                            <option value='remote-work'>Remote Work</option>
                        </select>
                    </p>
    
                    <div className='grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4'>
                        {templates.map(template => (
                            <div
                                key={template.id}
                                className='group shadow-md rounded-lg cursor-pointer'
                            >
                                <div
                                    className={`${template.color} rounded-lg h-32 relative overflow-hidden`}
                                >
                                    <div className='absolute top-2 left-2'>
                                        <span className='bg-yellow-400 text-yellow-900 text-xs font-medium px-2 py-0.5 rounded'>
                                            TEMPLATE
                                        </span>
                                    </div>
                                    <div className='absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all duration-200'></div>
                                </div>
                                <h3 className='mt-2 text-sm font-medium text-white group-hover:text-blue-600 p-2'>
                                    {template.title}
                                </h3>
                            </div>
                        ))}
                    </div>
    
                    <button className='mt-4 text-sm text-blue-600 hover:text-blue-700 hover:underline'>
                        Browse the full template gallery
                    </button>
                </div>
    
                {/* Recently Viewed */}
                <div className='mb-8'>
                    <div className='flex items-center mb-4'>
                        <svg
                            className='w-5 h-5 mr-2 text-white'
                            fill='none'
                            viewBox='0 0 24 24'
                            stroke='currentColor'
                        >
                            <path
                                strokeLinecap='round'
                                strokeLinejoin='round'
                                strokeWidth={2}
                                d='M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z'
                            />
                        </svg>
                        <h2 className='text-lg font-semibold text-white'>
                            Recently viewed
                        </h2>
                    </div>
    
                    <div className='grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4'>
                        {recentBoards.map(board => (
                            <div
                                key={board.id}
                                className='shadow-md group cursor-pointer'
                            >
                                <div
                                    className={`${board.color} rounded-lg h-24 relative overflow-hidden`}
                                >
                                    {board.title.includes('Template') && (
                                        <div className='absolute top-2 left-2'>
                                            <span className='bg-yellow-400 text-yellow-900 text-xs font-medium px-2 py-0.5 rounded'>
                                                TEMPLATE
                                            </span>
                                        </div>
                                    )}
                                    <div className='absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all duration-200'></div>
                                </div>
                                <h3 className='mt-2 p-2 text-sm font-medium text-white group-hover:text-blue-600'>
                                    {board.title}
                                </h3>
                            </div>
                        ))}
                    </div>
                </div>
    
                {/* Your Workspaces */}
                <div>
                    <h2 className='text-lg font-semibold text-white mb-4 flex items-center'>
                        YOUR WORKSPACES
                    </h2>
    
                    {/* Nashtech Workspace */}
                    <div className='mb-6'>
                        <div className='flex items-center justify-between mb-4'>
                            <div className='flex items-center'>
                                <div className='w-8 h-8 bg-orange-500 rounded flex items-center justify-center text-white font-semibold text-sm mr-3'>
                                    N
                                </div>
                                <h3 className='text-lg font-medium text-white'>
                                    Nashtech
                                </h3>
                            </div>
                            <div className='flex items-center space-x-2'>
                                <button className='flex items-center px-3 py-1.5 text-sm text-white hover:bg-[#A6C5E229] rounded'>
                                    <svg
                                        className='w-4 h-4 mr-1'
                                        fill='none'
                                        viewBox='0 0 24 24'
                                        stroke='currentColor'
                                    >
                                        <path
                                            strokeLinecap='round'
                                            strokeLinejoin='round'
                                            strokeWidth={2}
                                            d='M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z'
                                        />
                                    </svg>
                                    Members
                                </button>
                                <button className='flex items-center px-3 py-1.5 text-sm text-white hover:bg-[#A6C5E229] rounded'>
                                    <svg
                                        className='w-4 h-4 mr-1'
                                        fill='none'
                                        viewBox='0 0 24 24'
                                        stroke='currentColor'
                                    >
                                        <path
                                            strokeLinecap='round'
                                            strokeLinejoin='round'
                                            strokeWidth={2}
                                            d='M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z'
                                        />
                                        <path
                                            strokeLinecap='round'
                                            strokeLinejoin='round'
                                            strokeWidth={2}
                                            d='M15 12a3 3 0 11-6 0 3 3 0 016 0z'
                                        />
                                    </svg>
                                    Settings
                                </button>
                                <button className='flex items-center px-3 py-1.5 text-sm text-blue-600 hover:bg-blue-50 rounded'>
                                    <svg
                                        className='w-4 h-4 mr-1'
                                        fill='none'
                                        viewBox='0 0 24 24'
                                        stroke='currentColor'
                                    >
                                        <path
                                            strokeLinecap='round'
                                            strokeLinejoin='round'
                                            strokeWidth={2}
                                            d='M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z'
                                        />
                                    </svg>
                                    Upgrade
                                </button>
                            </div>
                        </div>
    
                        <div className='pb-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4'>
                            {workspaceBoards.map(board => (
                                <div
                                    key={board.id}
                                    className='group shadow-md cursor-pointer'
                                >
                                    <div
                                        className={`${board.color} rounded-lg h-24 relative overflow-hidden`}
                                    >
                                        <div className='absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all duration-200'></div>
                                    </div>
                                    <h3 className='mt-2 p-2 text-sm font-medium text-gray-900 group-hover:text-blue-600'>
                                        {board.title}
                                    </h3>
                                </div>
                            ))}
                            {/* Create new board card */}
                            <div className='group cursor-pointer'>
                                <div className='bg-[#282D33] hover:bg-gray-300 rounded-lg h-24 flex items-center justify-center transition-colors duration-200'>
                                    <div className='text-center'>
                                        <div className='text-gray-600 group-hover:text-gray-700'>
                                            Create new board
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
}

export default Boards;
