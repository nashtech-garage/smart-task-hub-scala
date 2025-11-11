import { userProfileService } from "@/services/userProfileService";
import type { RootState } from "@/store";
import { toggleTheme } from "@/store/slices/themeSlice";
import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { toast } from "react-toastify";

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
    const dispatch = useDispatch();
    const theme = useSelector((state: RootState) => state.theme.mode);

    const handleToggleTheme = async () => {
        try {
            await userProfileService.updateUserProfile({
                themeMode: theme === 'dark' ? 'light' : 'dark',
            });
            dispatch(toggleTheme());
        }
        catch (error) {
            console.error("Failed to toggle theme:", error);
            toast.error("Failed to toggle theme");
        }
    }

    return (
        <div className='absolute right-0 mt-2 w-80 bg-[var(--background)] rounded-lg shadow-lg border border-gray-600 z-50'>
            <div className='p-4'>
                <div className='text-xs text-[var(--menu-text)] uppercase tracking-wide font-semibold mb-3'>
                    ACCOUNT
                </div>

                <div className='flex items-center space-x-3 mb-4'>
                    <div className='w-10 h-10 bg-gradient-to-br from-yellow-400 to-orange-500 rounded-full flex items-center justify-center text-white font-semibold'>
                        {userName
                            ?.charAt(0)
                            .toUpperCase()}
                    </div>
                    <div>
                        <div className='font-medium text-[var(--menu-text)]'>
                            {userName}
                        </div>
                        <div className='text-sm text-[var(--menu-text)]'>
                            {email}
                        </div>
                    </div>
                </div>

                <div className='border-t border-gray-600'>
                    <div className='space-y-1'>
                        {/* <button className='w-full text-left px-3 py-2 text-sm text-[#B6C2CF] hover:bg-gray-700 rounded'>
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
                        </button> */}
                        <div
                            onClick={handleToggleTheme}
                            className="flex items-center gap-2 px-3 py-2 text-sm text-[var(--menu-text)] hover:bg-[var(--hover-bg)] rounded justify-between"
                        >
                            <span>Dark mode</span>
                            <div
                                className={`w-10 h-5 flex items-center rounded-full p-1 duration-300 ${theme === 'dark' ? 'bg-gray-600' : 'bg-gray-300'
                                    }`}
                            >
                                <div
                                    className={`bg-white w-4 h-4 rounded-full shadow-md transform duration-300 ${theme === 'dark' ? 'translate-x-5' : ''
                                        }`}
                                ></div>
                            </div>
                        </div>

                    </div>

                    <button
                        onClick={handleCreateWorkspace}
                        className='w-full text-left border-t border-gray-600 px-3 py-2 text-sm text-blue-600 hover:bg-[var(--hover-bg)] rounded flex items-center'>
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
                        className='w-full text-left px-3 py-2 text-sm text-[var(--menu-text)] hover:bg-[var(--hover-bg)] rounded'
                    >
                        Log out
                    </button>
                </div>
            </div>
        </div>
    )
}

export default ProfileDropDown;