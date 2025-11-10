import { completedBoard } from '@/services/workspaceService';
// import { Activity, Copy, EarthIcon, Eye, Folder, Globe, Image, Menu, Settings, Tag, Users, X } from 'lucide-react';
import { Download, Folder, Menu, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import ArchivedItemsModal from './ArchivedItemsModal';
import { deleteColumn, exportBoard, restoreColumn } from '@/services/boardService';
import { notify } from '@/services/toastService';
import taskService from '@/services/taskService';
import { useAppDispatch, useAppSelector, type RootState } from '@/store';
import { selectArchivedColumns } from '@/store/selectors/columnsSelector';
import { selectArchivedTasks } from '@/store/selectors/tasksSelectors';
import { fetchArchivedColumnsThunk } from '@/store/thunks/columnsThunks';
import { fetchArchivedTasksThunk } from '@/store/thunks/tasksThunks';
import { archivedTaskRestored } from '@/store/slices/archiveTasksSlice';
import { columnDeleted } from '@/store/slices/archiveColumnsSlice';
import { useSelector } from 'react-redux';

// const menuItems = [
//     { icon: <Users />, label: "Share", color: "text-gray-300" },
//     { icon: <EarthIcon />, label: "Visibility: Workspace", color: "text-gray-300" },
//     { icon: <Settings />, label: "Settings", color: "text-gray-300" },
//     { icon: <Image />, label: "Change background", color: "text-gray-300" },
// ];

// const powerUpItems = [
// { icon: <Tag />, label: "Labels", color: "text-gray-300" },
// { icon: <Activity />, label: "Activity", color: "text-gray-300" },
// ];

// const moreItems = [
//     { icon: <Eye />, label: "Watch", color: "text-gray-300"},
//     { icon: <Copy />, label: "Copy board", color: "text-gray-300"},
// ];

interface BoardNavbarProps {
    id: number;
    name?: string;
    isBoardClosed: boolean;
    setIsBoardClosed: (closed: boolean) => void;
}

const BoardNavbar: React.FC<BoardNavbarProps> = ({
    id,
    name,
    isBoardClosed,
    setIsBoardClosed
}) => {

    const { boardId } = useParams();
    const [showMenu, setShowMenu] = useState(false);
    const [showVisibility, setShowVisibility] = useState(false);
    const [showCloseConfirm, setShowCloseConfirm] = useState(false);
    const [showArchivedItems, setShowArchivedItems] = useState(false);
    const [isExporting, setIsExporting] = useState(false);
    const dispatch = useAppDispatch();
    const archivedColumns = useAppSelector(selectArchivedColumns);
    const archivedTasks = useAppSelector(selectArchivedTasks);

    // const members = useAppSelector(selectBoardMembers); // array member objects
    const members = useSelector((state: RootState) => state.members);

    const MAX_VISIBLE = 5;
    const visibleMembers = members.slice(0, MAX_VISIBLE);
    const extraCount = members.length - MAX_VISIBLE;

    useEffect(() => {
        if (id !== 0) {
            dispatch(fetchArchivedColumnsThunk(id));
            dispatch(fetchArchivedTasksThunk(id));
        }
    }, [id, dispatch]);

    const handleCloseMenu = () => {
        setShowCloseConfirm(false);
        setShowMenu(false);
        setShowArchivedItems(false);
    };

    const confirmCloseBoard = async () => {
        if (!boardId) return;
        try {
            await completedBoard(Number(boardId));
            handleCloseMenu();
            setIsBoardClosed(true);
        }
        catch (error: any) {
            notify.error(error.response?.data?.message);
        }
    };

    const cancelCloseBoard = () => {
        setShowCloseConfirm(false);
    };

    const handleArchivedItemsClick = () => {
        setShowArchivedItems(true);
    };

    const handleBackToMenu = () => {
        setShowArchivedItems(false);
        setShowMenu(true);
    };

    // Archive handlers - implement these based on your data management
    const handleRestoreTask = async (taskId: number) => {
        try {
            const result = await taskService.restoreTask(taskId);
            dispatch(archivedTaskRestored(taskId));
            notify.success(result.message);
        } catch (error: any) {
            notify.error(error.response?.data?.message);
        }
    };

    const handleRestoreColumn = async (columnId: number) => {
        try {
            const result = await restoreColumn(columnId);
            notify.success(result.message);
        } catch (error: any) {
            notify.error(error.response?.data?.message);
        }
    };

    const handleDeleteTask = async (taskId: number) => {
        try {
            const result = await taskService.deleteTask(taskId);
            // dispatch(archivedTaskDeleted(taskId));
            notify.success(result.message);
        } catch (error: any) {
            notify.error(error.response?.data?.message);
        }
    };

    const handleDeleteColumn = async (columnId: number) => {
        try {
            const result = await deleteColumn(columnId);
            dispatch(columnDeleted(columnId));
            notify.success(result.message);
        } catch (error: any) {
            notify.error(error.response?.data?.message);
        }
    };

    const handleExportBoard = async () => {
        if (isExporting) return;
        try {
            setIsExporting(true);
            notify.info("Exporting data...");
            const response = await exportBoard(Number(boardId));

            // convert data 
            const jsonString = JSON.stringify(response.data, null, 2);

            // create blob from json
            const blob = new Blob([jsonString], { type: "application/json" });

            // create temporary link
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = `${name}.json`;
            document.body.appendChild(a);
            a.click();

            // clean up
            a.remove();
            URL.revokeObjectURL(url);
        } catch (err) {
            console.error("Export failed:", err);
            notify.error("Failed to export board. Please try again!");
        }
        finally {
            setIsExporting(false);
        }
    };

    return (
        <div className={`${isBoardClosed ? 'pointer-events-none opacity-60' : ''} h-[50px] flex items-center justify-between bg-[var(--board-navbar-bg)] p-4`}>
            <h1 className='text-xl font-bold text-[var(--foreground)] mb-2'>
                {name}
            </h1>
            {/* Folder Menu Button */}
            <div className="relative flex items-center gap-2">
                {/* <button
                    onClick={() => setShowVisibility(!showVisibility)}
                    className="flex items-center justify-center w-8 h-8 text-white hover:bg-[#3A4150] rounded transition-colors"
                >
                    <Globe className="w-5 h-5" />
                </button> */}

                {/* Avatars */}
                {visibleMembers.map((m) => (
                    <div
                        key={m.id}
                        title={m.name}
                        className="w-8 h-8 rounded-full bg-gray-600 text-white flex items-center justify-center text-xs font-medium overflow-hidden"
                    >
                        {m.name.charAt(0).toUpperCase()}
                    </div>
                ))}

                {/* +n if there are more than 5 members */}
                {extraCount > 0 && (
                    <div className="w-8 h-8 rounded-full bg-gray-500 text-white flex items-center justify-center text-xs font-medium">
                        +{extraCount}
                    </div>
                )}

                <button
                    onClick={() => setShowMenu(!showMenu)}
                    className="flex items-center justify-center w-8 h-8 text-[var(--foreground)] hover:bg-[var(--board-navbar-menu-hover)] rounded transition-colors"
                >
                    <Menu className="w-5 h-5" />
                </button>

                {/* Menu Modal */}
                {showMenu && (
                    <>
                        {/* Backdrop */}
                        <div
                            className="fixed inset-0 z-40"
                            onClick={handleCloseMenu}
                        />

                        {/* Menu */}
                        {
                            !showArchivedItems ?
                                <div className="absolute right-0 top-10 w-80 bg-[var(--background)] rounded-lg shadow-xl z-50 border border-gray-600">
                                    {/* Menu Header */}
                                    <div className="flex items-center justify-between p-3 border-b border-gray-600">
                                        <span className="text-[var(--menu-text)] font-medium text-sm">Menu</span>
                                        <button
                                            onClick={() => setShowMenu(false)}
                                            className="text-gray-400 hover:text-white transition-colors"
                                        >
                                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                            </svg>
                                        </button>
                                    </div>

                                    {/* Menu Content */}
                                    <div className="p-2">
                                        {/* Main Menu Items */}
                                        {/* {menuItems.map((item, index) => (
                                            <button
                                                key={index}
                                                className="w-full flex items-center px-3 py-2 text-sm hover:bg-[#34495e] transition-colors text-left"
                                            >
                                                <span className="mr-3 text-base text-white">{item.icon}</span>
                                                <div className="flex-1">
                                                    <div className={`${item.color}`}>
                                                        {item.label}
                                                    </div>
                                                </div>
                                            </button>
                                        ))} */}

                                        {/* Power-Ups Section */}
                                        <button
                                            onClick={handleArchivedItemsClick}
                                            className="w-full flex items-center px-3 py-2 text-sm hover:bg-[var(--hover-bg)] transition-colors text-left"
                                        >
                                            <span className="mr-3 text-base text-[var(--menu-text)]"><Folder /></span>
                                            <div className="flex-1">
                                                <div className='text-[var(--menu-text)]'>
                                                    Archived items
                                                </div>
                                            </div>
                                        </button>

                                        <button
                                            onClick={handleExportBoard}
                                            className="w-full flex items-center px-3 py-2 text-sm hover:bg-[var(--hover-bg)] transition-colors text-left"
                                        >
                                            <span className="mr-3 text-base text-[var(--menu-text)]"><Download /></span>
                                            <div className="flex-1">
                                                <div className='text-[var(--menu-text)]'>
                                                    Export as JSON
                                                </div>
                                            </div>
                                        </button>

                                        {/* Divider */}
                                        <div className="border-t border-gray-600 my-2"></div>

                                        {/* More Items */}
                                        {/* {moreItems.map((item, index) => (
                                        <button
                                            key={index}
                                            className="w-full flex items-center px-3 py-2 text-sm hover:bg-[#34495e] transition-colors text-left"
                                        >
                                            <span className="mr-3 text-base text-white">{item.icon}</span>
                                            <div className="flex-1">
                                                <div className={item.color}>
                                                    {item.label}
                                                </div>
                                            </div>
                                        </button>
                                    ))} */}
                                        {/* Close Board Item - Separate with relative positioning for popup */}
                                        <div className="relative">
                                            <button
                                                onClick={() => setShowCloseConfirm(true)}
                                                className="w-full flex items-center px-3 py-2 text-sm hover:bg-[var(--hover-bg)] transition-colors text-left"
                                            >
                                                <span className="mr-3 text-base text-[var(--menu-text)]"><X /></span>
                                                <div className="flex-1">
                                                    <div className="text-red-400">
                                                        Close board
                                                    </div>
                                                </div>
                                            </button>

                                            {/* Close Board Confirmation Popup - Positioned above the button */}
                                            {showCloseConfirm && (
                                                <>
                                                    {/* Confirmation Popup */}
                                                    <div className="absolute bottom-full right-0 bg-[#1a1a1a] rounded p-3">
                                                        <div className="flex items-center justify-between pb-2 border-b border-gray-600">
                                                            <h3 className="text-white font-semibold text-sm">Close board?</h3>
                                                            <button
                                                                onClick={cancelCloseBoard}
                                                                className="text-gray-400 hover:text-white transition-colors rounded"
                                                            >
                                                                <X className="w-4 h-4 cursor-pointer" />
                                                            </button>
                                                        </div>
                                                        <p className="text-gray-300 text-xs mb-3 pt-2 leading-relaxed">
                                                            You can find and reopen closed boards at the bottom of{' '}
                                                            <span className="text-blue-400 underline cursor-pointer">your boards page</span>.
                                                        </p>
                                                        <button
                                                            onClick={confirmCloseBoard}
                                                            className="w-full bg-red-500 hover:bg-red-600 text-white py-2 rounded text-sm font-medium transition-colors"
                                                        >
                                                            Close
                                                        </button>
                                                    </div>
                                                </>
                                            )}
                                        </div>
                                    </div>
                                </div>
                                :
                                <>
                                    {/* Archived Items Modal */}
                                    <ArchivedItemsModal
                                        onClose={handleCloseMenu}
                                        onBack={handleBackToMenu}
                                        archivedTasks={archivedTasks}
                                        archivedColumns={archivedColumns}
                                        onRestoreTask={handleRestoreTask}
                                        onRestoreColumn={handleRestoreColumn}
                                        onDeleteTask={handleDeleteTask}
                                        onDeleteColumn={handleDeleteColumn}
                                    />
                                </>
                        }

                    </>
                )}

                {/* Visibility Popup */}
                {showVisibility && (
                    <>
                        {/* Backdrop */}
                        <div
                            className="fixed inset-0 z-40"
                            onClick={() => setShowVisibility(false)}
                        />
                        <div className="absolute right-0 top-12 w-72 bg-[#1F2532] text-white rounded-lg shadow-lg p-4 z-50">
                            <h2 className="text-lg font-semibold mb-2">Change visibility</h2>
                            <ul className="space-y-3 text-sm">
                                <li className="flex items-start space-x-2 cursor-pointer hover:bg-[#3A4150] p-2 rounded">
                                    <span>🔒</span>
                                    <div>
                                        <p className="font-medium">Private</p>
                                        <p className="text-gray-400 text-xs">
                                            Only board members can see this board.
                                        </p>
                                    </div>
                                </li>
                                <li className="flex items-start space-x-2 cursor-pointer hover:bg-[#3A4150] p-2 rounded">
                                    <span>👥</span>
                                    <div>
                                        <p className="font-medium">Workspace</p>
                                        <p className="text-gray-400 text-xs">
                                            All workspace members can see and edit.
                                        </p>
                                    </div>
                                </li>
                                <li className="flex items-start space-x-2 cursor-pointer hover:bg-[#3A4150] p-2 rounded">
                                    <span>🏢</span>
                                    <div>
                                        <p className="font-medium">Organization</p>
                                        <p className="text-gray-400 text-xs">
                                            All org members can see this board.
                                        </p>
                                    </div>
                                </li>
                                <li className="flex items-start space-x-2 cursor-pointer hover:bg-[#3A4150] p-2 rounded">
                                    <span>🌍</span>
                                    <div>
                                        <p className="font-medium">Public</p>
                                        <p className="text-gray-400 text-xs">
                                            Anyone on the internet can see this board.
                                        </p>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}

export default BoardNavbar;