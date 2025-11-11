import { SearchContext } from '@/components/layout/MainLayout';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { fetchAllBoards } from '@/services/boardService';
import type { Board, TaskSearchResponse } from '@/types';
import { Check, StickyNote } from 'lucide-react';
import { useContext, useEffect, useRef, useState } from 'react'
import { formatDistanceToNow } from "date-fns";
import { useDebounce } from 'use-debounce';
import taskService from '@/services/taskService';
import { useNavigate } from 'react-router-dom';


interface DisplayBoard {
    isChoosen: boolean;
    detail: Board;
}

const SearchPage = () => {
    const searchContext = useContext(SearchContext);
    const [isLoading, setIsLoading] = useState(false);
    const [boards, setBoards] = useState<Board[]>([]);
    const [displayBoards, setDisplayBoards] = useState<DisplayBoard[]>([]);
    const [boardSearch, setBoardSearch] = useState("");
    const [showBoardDropdown, setShowBoardDropdown] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const [keyword, setKeyword] = useState("");
    const [debounceQuery] = useDebounce(keyword, 500);
    const [tasks, setTasks] = useState<TaskSearchResponse[]>([]);
    const [page, setPage] = useState(1);
    const [hasMore, setHasMore] = useState(false);
    const size = 10;
    const navigate = useNavigate();

    if (!searchContext) return null;
    const { setShowSearch } = searchContext;

    const fetchBoards = async () => {
        try {
            setIsLoading(true);
            const res = await fetchAllBoards();
            setBoards(res.data);
            setDisplayBoards(res.data.slice(0, 3).map(b => ({ isChoosen: false, detail: b })));
        }
        catch (error) {
            console.error('Error fetching boards:', error);
        }
        finally {
            setIsLoading(false);
        }
    }

    const fetchTasks = async () => {
        try {
            setIsLoading(true);
            const chosenIds = displayBoards.filter(b => b.isChoosen).map(b => b.detail.id);
            const res = await taskService.searchTasks(debounceQuery, chosenIds, page, size);
            setTasks(prev => page === 1 ? res.data : [...prev, ...res.data]);
            if (res.data.length === size) {
                setHasMore(true);
            }
            else {
                setHasMore(false);
            }
        } catch (error) {
            console.error("Error searching tasks:", error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleClickOutside = (event: MouseEvent) => {
        if (
            dropdownRef.current &&
            !dropdownRef.current.contains(event.target as Node)
        ) {
            setShowBoardDropdown(false);
        }
    };

    useEffect(() => {
        setShowSearch(false);
        fetchBoards();
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    useEffect(() => {
        fetchTasks();
    }, [debounceQuery, displayBoards, page]);

    const toggleBoard = (board: DisplayBoard) => {
        setDisplayBoards(prev => {
            const defaultBoardIds = boards.slice(0, 3).map(b => b.id);
            const isDefaultBoard = defaultBoardIds.includes(board.detail.id);
            if (isDefaultBoard) {
                return prev.map(b => b.detail.id === board.detail.id ? { ...b, isChoosen: !b.isChoosen } : b);
            }
            return prev.filter(b => b.detail.id !== board.detail.id);
        });
    }

    return (
        <div className="flex w-full h-full bg-[var(--background)] text-[var(--foreground)]">
            {isLoading ? (
                <LoadingSpinner />
            ) : (
                <>
                    {/* Sidebar filter */}
                    <aside className="w-64 border-r border-gray-700 p-4 overflow-y-auto">
                        {/* Sort */}
                        {/* <div className="mb-6">
                    <h3 className="text-xs font-semibold text-gray-400 uppercase mb-2">
                        Sort card results
                    </h3>
                    <button className="px-3 py-1 rounded-full bg-gray-700 text-sm">
                        By last updated
                    </button>
                </div> */}

                        {/* Filter by last updated */}
                        {/* <div className="mb-6">
                    <h3 className="text-xs font-semibold text-gray-400 uppercase mb-2">
                        Filter cards by last updated
                    </h3>
                    <div className="flex flex-wrap gap-2">
                        {["Last 24 hours", "Last week", "Last month", "Last year"].map(
                            (label) => (
                                <button
                                    key={label}
                                    className="px-3 py-1 rounded-full bg-gray-700 text-sm"
                                >
                                    {label}
                                </button>
                            )
                        )}
                    </div>
                </div> */}

                        {/* Filter by board */}
                        <div className="mb-6">
                            <h3 className="text-xs font-semibold text-gray-400 uppercase mb-2">
                                Filter cards by board
                            </h3>

                            {/* Top 3 boards + chosen boards */}
                            <div className="flex flex-wrap gap-2 mb-2">
                                {displayBoards.map((board) => (
                                    <span
                                        key={board.detail.id}
                                        className={`px-3 py-1 rounded-full text-sm cursor-pointer flex items-center gap-1 border
                                            ${board.isChoosen ? "bg-blue-600" : "bg-[var(--background)] hover:bg-[var(--hover-bg)]"}`}
                                        onClick={() => toggleBoard(board)}
                                    >
                                        {board.isChoosen && <Check size={14} />}
                                        {board.detail.name}
                                    </span>
                                ))}
                            </div>

                            {/* Dropdown search */}
                            <div className="relative" ref={dropdownRef}>
                                <input
                                    type="text"
                                    placeholder="Find a board"
                                    className="w-full px-3 py-1 rounded-md bg-[var(--background)] border text-sm focus:outline-none"
                                    onFocus={() => setShowBoardDropdown(true)}
                                    value={boardSearch}
                                    onChange={(e) => setBoardSearch(e.target.value)}
                                />

                                {showBoardDropdown && (
                                    <div className="absolute z-50 mt-1 w-full max-h-60 overflow-y-auto bg-[var(--background)] border border-gray-700 rounded-md shadow-lg">
                                        {boards
                                            .filter((b) => {
                                                const isAlreadyChosen = displayBoards.some(
                                                    (db) => db.detail.id === b.id
                                                );
                                                if (isAlreadyChosen) return false;
                                                return b.name.toLowerCase().includes(boardSearch.toLowerCase())
                                            })
                                            .map((b) => {
                                                return (
                                                    <div
                                                        key={b.id}
                                                        className="px-3 py-2 text-sm cursor-pointer flex items-center gap-2 hover:bg-[var(--hover-bg)]"
                                                        onClick={() => setDisplayBoards(prev => [...prev, { isChoosen: true, detail: b }])}
                                                    >
                                                        {b.name}
                                                    </div>
                                                );
                                            })}
                                    </div>
                                )}
                            </div>
                        </div>


                        {/* Other filters */}
                        {/* <div className="mb-6">
                    <h3 className="text-xs font-semibold text-gray-400 uppercase mb-2">
                        Closed boards and archived cards
                    </h3>
                    <button className="px-3 py-1 rounded-full bg-gray-700 text-sm">
                        Do not show Closed boards and Archived cards
                    </button>
                </div> */}
                    </aside>

                    {/* Results */}
                    <main className="flex-1 p-6 overflow-y-auto">
                        {/* Search bar */}
                        <div className="mb-6">
                            <input
                                type="text"
                                placeholder="Search"
                                className="w-full px-4 py-2 rounded-md bg-[var(--background)] border border-gray-700 focus:outline-none"
                                value={keyword}
                                onChange={(e) => setKeyword(e.target.value)}
                            />
                        </div>

                        {/* Cards */}
                        <h2 className="text-lg font-semibold mb-4">Cards</h2>
                        <div className="space-y-4">
                            {tasks.map((card) => (
                                <div
                                    key={card.taskId}
                                    className="flex items-start p-3 rounded-md hover:bg-[var(--hover-bg)] cursor-pointer"
                                    onClick={() => {
                                        setShowSearch(true);
                                        navigate(`/board/${card.projectId}`);
                                    }}
                                >
                                    <div className="flex-shrink-0 text-gray-400 mt-1">
                                        <StickyNote />
                                    </div>
                                    <div className="ml-3 flex-1">
                                        <div className="flex justify-between">
                                            <span className="font-medium">{card.taskName}</span>
                                            <span className="text-xs text-gray-400">
                                                Updated {formatDistanceToNow(new Date(card.updatedAt), { addSuffix: true })}
                                            </span>
                                        </div>
                                        <div className="text-sm text-gray-400">
                                            {card.projectName}: {card.columnName} {card.taskStatus === 'archived' && `• ${card.taskStatus}`}
                                        </div>
                                        {card.taskDescription && (
                                            <div className="text-xs text-gray-500">{card.taskDescription}</div>
                                        )}
                                    </div>
                                </div>
                            ))}

                            {tasks.length === 0 && (
                                <div className="text-sm text-gray-400">No results found.</div>
                            )}
                        </div>

                        {/* Pagination */}
                        {hasMore && (
                            <div className="mt-6 flex justify-center space-x-4">
                                <button
                                    className='cursor-pointer text-blue-400 hover:text-white'
                                    onClick={() => setPage(prev => prev + 1)}
                                >
                                    Show more
                                </button>
                            </div>
                        )}

                    </main>
                </>
            )
            }

        </div >
    );
}

export default SearchPage