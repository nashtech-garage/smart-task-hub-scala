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

export default ProfileDropDown;