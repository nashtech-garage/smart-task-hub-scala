import { notify } from '@/services/toastService';
import { getWorkspaces } from '@/services/workspaceService';
import type { WorkSpace } from '@/types/workspace';
import clsx from 'clsx';
import { Folders, House, LayoutTemplate, ChevronDown, ChevronRight, Settings } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';

const navigationItems = [
    {
        id: 'nav-item1',
        icon: <Folders />,
        title: 'Boards',
        navigate: '/user/boards',
    },
    {
        id: 'nav-item2',
        icon: <LayoutTemplate />,
        title: 'Templates',
        navigate: '/user/templates',
    },
    {
        id: 'nav-item3',
        icon: <House />,
        title: 'Home',
        navigate: '/user/home',
    },
];

const workspaceItems = [
    {
        id: 'w-item1',
        icon: <Folders />,
        title: 'Boards',
        navigate: '/workspace/boards'
    },
    // {
    //     id: 'w-item2',
    //     icon: <Users />,
    //     title: 'Members',
    //     navigate: '/workspace/members'
    // },
    {
        id: 'w-item3',
        icon: <Settings />,
        title: 'Setting ',
        navigate: '/workspace/setting'
    },
]

const Sidebar = () => {
    const location = useLocation();
    const [expandedWorkspaceIds, setExpandedWorkspaceIds] = useState<number[]>([]);
    const [workspaces, setWorkSpaces] = useState<WorkSpace[]>([]);

    const fetchWorkspaces = async () => {
        await getWorkspaces()
            .then(data => {
                if (!data.data) return;
                setWorkSpaces([...data.data]);
            })
            .catch(err => notify.error(err?.message))
    }

    useEffect(() => {
        // Load expanded workspace IDs from localStorage
        const saved = localStorage.getItem('expandedWorkspaceIds');
        if (saved) {
            try {
                const parsedIds = JSON.parse(saved);
                if (Array.isArray(parsedIds)) {
                    setExpandedWorkspaceIds(parsedIds);
                }
            } catch (error) {
                console.error('Failed to parse expandedWorkspaceIds from localStorage:', error);
            }
        }

        fetchWorkspaces()
    }, []);

    const handleExpandWorkspace = (workspaceId: number | undefined) => {
        if (!workspaceId) return;
        setExpandedWorkspaceIds(prev => {
            let newExpandedIds;

            if (prev.includes(workspaceId)) {
                // Remove the workspace ID if it's already expanded
                newExpandedIds = prev.filter(id => id !== workspaceId);
            } else {
                // Add the workspace ID if it's not expanded
                newExpandedIds = [...prev, workspaceId];
            }

            // Store in localStorage
            localStorage.setItem('expandedWorkspaceIds', JSON.stringify(newExpandedIds));

            return newExpandedIds;
        });
    };

    return (
        <div className='w-64 flex flex-col'>
            <div className='p-4'>
                {/* Navigation Items */}
                <nav className='space-y-1 border-b py-2'>
                    {navigationItems.map(item => (
                        <Link
                            to={item.navigate}
                            key={item.id}
                            className={clsx(
                                'flex items-center gap-2 px-3 py-2 text-sm hover:bg-[(var(--hover-bg))] font-medium rounded-lg transition-colors duration-200',
                                {
                                    'text-[#155DFC] bg-[#A6C5E229]':
                                        item.navigate === location.pathname,
                                }
                            )}
                        >
                            {item.icon}
                            {item.title}
                        </Link>
                    ))}
                </nav>
            </div>

            {/* Workspaces Section */}
            <div className='flex-1 px-4 overflow-y-auto'>
                <div className='mb-4'>
                    <h3 className='text-xs font-semibold uppercase tracking-wide mb-2'>
                        Workspaces
                    </h3>
                    {workspaces.map(workspace => (
                        <div className='space-y-1 mb-2' key={workspace.id}>
                            {/* Workspace Header */}
                            <div
                                className='flex items-center justify-between p-2 hover:bg-[(var(--hover-bg))] rounded-lg cursor-pointer transition-colors duration-200'
                                onClick={() => handleExpandWorkspace(workspace.id)}
                            >
                                <div className='flex items-center'>
                                    <div className='w-8 h-8 bg-orange-500 rounded flex items-center justify-center text-white font-semibold text-sm mr-3'>
                                        {workspace.name.charAt(0)}
                                    </div>
                                    <span className='text-sm font-medium'>
                                        {workspace.name}
                                    </span>
                                </div>
                                <div className='transition-transform duration-200'>
                                    {workspace.id && expandedWorkspaceIds.includes(workspace.id) ? (
                                        <ChevronDown className='w-4 h-4 text-gray-400' />
                                    ) : (
                                        <ChevronRight className='w-4 h-4 text-gray-400' />
                                    )}
                                </div>
                            </div>

                            {/* Workspace Submenu with Animation */}
                            <div
                                className={clsx(
                                    'overflow-hidden transition-all duration-300 ease-in-out',
                                    workspace.id && expandedWorkspaceIds.includes(workspace.id)
                                        ? 'max-h-32 opacity-100'
                                        : 'max-h-0 opacity-0'
                                )}
                            >
                                <div className='space-y-1 pt-1'>
                                    {
                                        workspaceItems.map(item => (
                                            <Link
                                                key={item.id}
                                                to={`${item.navigate}/${workspace.id}`}
                                                className='flex items-center gap-2 pl-11 px-3 py-1.5 text-xs hover:bg-[(var(--hover-bg))] rounded transition-colors duration-200'

                                            >
                                                {item.icon}
                                                {item.title}
                                            </Link>
                                        ))
                                    }
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Sidebar;