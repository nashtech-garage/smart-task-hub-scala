import BoardNavbar from '@/components/board/BoardNavbar';
import DroppableColumn from '@/components/board/DroppableColumn';
import TaskDetailModal from '@/components/board/TaskDetailModal';
import LoadingContent from '@/components/ui/LoadingContent';
import { archiveColumn, createNewColumn, fetchBoardColumns, fetchBoardDetail, updateColumn, updateColumnPosititon } from '@/services/boardService';
import taskService from '@/services/taskService';
import { notify } from '@/services/toastService';
import { connectToProjectWS, disconnectWS } from '@/services/websocketService';
import { reopenBoard } from '@/services/workspaceService';
import type { Board, Column } from '@/types';
import {
    DndContext,
    DragOverlay,
    KeyboardCode,
    KeyboardSensor,
    PointerSensor,
    pointerWithin,
    useSensor,
    useSensors,
    type DragEndEvent,
    type DragOverEvent,
    type DragStartEvent,
    type UniqueIdentifier,
} from '@dnd-kit/core';
import {
    arrayMove,
    horizontalListSortingStrategy,
    SortableContext,
    sortableKeyboardCoordinates,
} from '@dnd-kit/sortable';
import { GripVertical, Plus, Lock, Unlock } from 'lucide-react';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

const WorkspaceBoard = () => {

    const { boardId } = useParams();
    const [isBoardClosed, setIsBoardClosed] = useState(false);
    const [boardDetail, setBoardDetail] = useState<Board>({ id: 0, name: '', status: undefined });
    const [columns, setColumns] = useState<Column[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [activeId, setActiveId] = useState<UniqueIdentifier | null>(null);
    const [activeInputColumnId, setActiveInputColumnId] = useState<number | null>(null);
    const [cardTitle, setCardTitle] = useState('');
    const [showDetailModal, setShowDetailModal] = useState(false);
    const containerRef = useRef<HTMLDivElement>(null);
    const dispatch = useDispatch();

    // OPTIMIZATION: Track dragging state separately from active elements
    const [isDragging, setIsDragging] = useState(false);
    const [dragType, setDragType] = useState<'column' | 'item' | null>(null);

    // OPTIMIZATION: Debounce drag operations
    const dragTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const lastDragOperationRef = useRef<string>('');

    // OPTIMIZATION: Store temporary drag state separately
    const dragStateRef = useRef<{
        originalColumns: Column[];
        currentColumns: Column[];
        hasChanged: boolean;
    }>({
        originalColumns: [],
        currentColumns: [],
        hasChanged: false,
    });

    const sensors = useSensors(
        useSensor(PointerSensor, {
            activationConstraint: {
                distance: 8,
            },
        }),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        }),
        useSensor(KeyboardSensor, {
            keyboardCodes: {
                start: [KeyboardCode.Enter],
                cancel: [KeyboardCode.Esc],
                end: [KeyboardCode.Enter],
            },
        })
    );

    // Memoize column IDs to prevent unnecessary SortableContext rerenders
    const columnIds = useMemo(() => columns.map(col => col.id), [columns]);

    // OPTIMIZATION: Memoize active elements only when activeId changes
    const activeElements = useMemo(() => {
        if (!activeId) return { activeColumn: null, activeItem: null };

        const activeColumn = columns.find(col => col.id === activeId);
        const activeItem = activeColumn
            ? null
            : columns
                .flatMap(col => col.tasks)
                .find(item => item.id === activeId);

        return { activeColumn, activeItem };
    }, [activeId, columns]);

    const fetchBoardData = async () => {
        setIsLoading(true);
        try {
            const boardData = await fetchBoardDetail(Number(boardId));
            setBoardDetail(boardData.data);
            setIsBoardClosed(boardData.data?.status === 'completed');
            if (boardData.data?.status === 'active') {
                const columnsData = await fetchBoardColumns(Number(boardId));
                setColumns(columnsData.data);
            }
        } catch (error: any) {
            notify.error(error.response?.data?.message);
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        fetchBoardData();
        connectToProjectWS(Number(boardId), (message) => {
            switch (message.type) {
                case "columnMoved":
                    console.log("Column moved", message);
                    break;

                default:
                    console.warn("Unknown WS event", message);
            }
        });

        return () => {
            disconnectWS();
        };
    }, [boardId, dispatch]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (
                containerRef.current &&
                !containerRef.current.contains(event.target as Node)
            ) {
                setActiveInputColumnId(null);
                setCardTitle('');
            }
        };

        if (activeInputColumnId && !isBoardClosed) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [activeInputColumnId, isBoardClosed]);

    const handleDragStart = useCallback(
        (event: DragStartEvent) => {
            // Prevent drag operations when board is closed
            if (isBoardClosed) return;

            // const activatorEvent = event.activatorEvent as KeyboardEvent | undefined;
            // if (activatorEvent?.key === ' ') return; 

            setActiveId(event.active.id);
            setIsDragging(true);

            // Determine drag type
            const type = event.active.data.current?.type as 'column' | 'item';
            setDragType(type);

            // Store initial state
            dragStateRef.current = {
                originalColumns: columns,
                currentColumns: columns,
                hasChanged: false,
            };

            // Close any active input when starting drag
            setActiveInputColumnId(null);
            setCardTitle('');
        },
        [columns, isBoardClosed]
    );

    // OPTIMIZATION: Heavily optimized drag over with batching and debouncing
    const handleDragOver = useCallback(
        (event: DragOverEvent) => {
            const { active, over } = event;

            if (!over || !isDragging || isBoardClosed) return;

            const activeId = active.id;
            const overId = over.id;

            if (activeId === overId) return;

            // OPTIMIZATION: Create operation signature to prevent duplicate operations
            const operationSignature = `${activeId}-${overId}`;
            if (lastDragOperationRef.current === operationSignature) {
                return;
            }
            lastDragOperationRef.current = operationSignature;

            // OPTIMIZATION: Clear previous timeout and batch operations
            if (dragTimeoutRef.current) {
                clearTimeout(dragTimeoutRef.current);
            }

            // OPTIMIZATION 10: Batch drag operations with timeout
            dragTimeoutRef.current = setTimeout(() => {
                const isActiveAnItem = active.data.current?.type === 'item';
                const isOverAnItem = over.data.current?.type === 'item';
                const isOverAColumn = over.data.current?.type === 'column';

                if (!isActiveAnItem) return;

                // Work with current drag state instead of component state
                const newColumns = [...dragStateRef.current.currentColumns];
                let hasChanged = false;

                // Item over item (different column)
                if (isActiveAnItem && isOverAnItem) {
                    const activeColumnIndex = newColumns.findIndex(col =>
                        col.tasks.find(item => item.id === activeId)
                    );
                    const overColumnIndex = newColumns.findIndex(col =>
                        col.tasks.find(item => item.id === overId)
                    );

                    if (
                        activeColumnIndex !== -1 &&
                        overColumnIndex !== -1 &&
                        activeColumnIndex !== overColumnIndex
                    ) {
                        const activeColumn = {
                            ...newColumns[activeColumnIndex],
                        };
                        const overColumn = { ...newColumns[overColumnIndex] };

                        const activeItemIndex = activeColumn.tasks.findIndex(
                            item => item.id === activeId
                        );
                        const overItemIndex = overColumn.tasks.findIndex(
                            item => item.id === overId
                        );

                        if (activeItemIndex !== -1 && overItemIndex !== -1) {
                            activeColumn.tasks = [...activeColumn.tasks];
                            overColumn.tasks = [...overColumn.tasks];

                            const [activeItem] = activeColumn.tasks.splice(
                                activeItemIndex,
                                1
                            );
                            overColumn.tasks.splice(
                                overItemIndex,
                                0,
                                activeItem
                            );

                            newColumns[activeColumnIndex] = activeColumn;
                            newColumns[overColumnIndex] = overColumn;
                            hasChanged = true;
                        }
                    }
                }

                // Item over column
                if (isActiveAnItem && isOverAColumn) {
                    const activeColumnIndex = newColumns.findIndex(col =>
                        col.tasks.find(item => item.id === activeId)
                    );
                    const overColumnIndex = newColumns.findIndex(
                        col => col.id === overId
                    );

                    if (
                        activeColumnIndex !== -1 &&
                        overColumnIndex !== -1 &&
                        activeColumnIndex !== overColumnIndex
                    ) {
                        const activeColumn = {
                            ...newColumns[activeColumnIndex],
                        };
                        const overColumn = { ...newColumns[overColumnIndex] };

                        const activeItemIndex = activeColumn.tasks.findIndex(
                            item => item.id === activeId
                        );

                        if (activeItemIndex !== -1) {
                            activeColumn.tasks = [...activeColumn.tasks];
                            overColumn.tasks = [...overColumn.tasks];

                            const [activeItem] = activeColumn.tasks.splice(
                                activeItemIndex,
                                1
                            );
                            overColumn.tasks.push(activeItem);

                            newColumns[activeColumnIndex] = activeColumn;
                            newColumns[overColumnIndex] = overColumn;
                            hasChanged = true;
                        }
                    }
                }

                // OPTIMIZATION: Only update state if something actually changed
                if (hasChanged) {
                    dragStateRef.current.currentColumns = newColumns;
                    dragStateRef.current.hasChanged = true;

                    // Update component state less frequently
                    setColumns(newColumns);
                }
            }, 16); // OPTIMIZATION: 16ms delay = ~60fps batching
        },
        [isDragging, isBoardClosed]
    );

    const handleDragEnd = useCallback((event: DragEndEvent) => {
        const { active, over } = event;

        // OPTIMIZATION: Clear timeout and reset refs
        if (dragTimeoutRef.current) {
            clearTimeout(dragTimeoutRef.current);
            dragTimeoutRef.current = null;
        }

        setActiveId(null);
        setIsDragging(false);
        setDragType(null);
        lastDragOperationRef.current = '';

        // If board is closed, restore original state and return
        if (isBoardClosed) {
            setColumns(dragStateRef.current.originalColumns);
            return;
        }

        if (!over) {
            // Restore original state if cancelled
            setColumns(dragStateRef.current.originalColumns);
            return;
        }

        const activeId = active.id;
        const overId = over.id;

        const isActiveAColumn = active.data.current?.type === 'column';
        const isOverAColumn = over.data.current?.type === 'column';
        const isActiveAnItem = active.data.current?.type === 'item';
        const isOverAnItem = over.data.current?.type === 'item';

        // Handle column reordering
        if (isActiveAColumn && isOverAColumn) {
            setColumns(columns => {
                const activeColumnIndex = columns.findIndex(
                    col => col.id === activeId
                );
                const overColumnIndex = columns.findIndex(
                    col => col.id === overId
                );

                if (activeColumnIndex !== -1 && overColumnIndex !== -1) {
                    const newColumns = arrayMove(columns, activeColumnIndex, overColumnIndex);
                    const movedIndex = overColumnIndex;
                    const preCol = newColumns[movedIndex - 1] || null;
                    const nextCol = newColumns[movedIndex + 1] || null;

                    let newPosition: number;
                    if (preCol && nextCol) {
                        newPosition = (preCol.position + nextCol.position) / 2;
                    } else if (!preCol && nextCol) {
                        newPosition = nextCol.position - 1000;
                    } else if (preCol && !nextCol) {
                        newPosition = preCol.position + 1000;
                    } else {
                        newPosition = 1000;
                    }

                    const movedColumn = newColumns[movedIndex];
                    updateColumnPosititon(Number(boardId), movedColumn.id, Math.floor(newPosition));
                    return newColumns;
                };
                return columns;
            });
        }

        // Handle item reordering within same column
        if (isActiveAnItem && isOverAnItem) {
            setColumns(prevColumns => {
                const activeColumnIndex = prevColumns.findIndex(col =>
                    col.tasks.find(item => item.id === activeId)
                );
                const overColumnIndex = prevColumns.findIndex(col =>
                    col.tasks.find(item => item.id === overId)
                );

                if (
                    activeColumnIndex === overColumnIndex &&
                    activeColumnIndex !== -1
                ) {
                    const newColumns = [...prevColumns];
                    const column = { ...newColumns[activeColumnIndex] };

                    const activeItemIndex = column.tasks.findIndex(
                        item => item.id === activeId
                    );
                    const overItemIndex = column.tasks.findIndex(
                        item => item.id === overId
                    );

                    if (activeItemIndex !== -1 && overItemIndex !== -1) {
                        column.tasks = arrayMove(
                            column.tasks,
                            activeItemIndex,
                            overItemIndex
                        );
                        newColumns[activeColumnIndex] = column;
                        return newColumns;
                    }
                }

                return prevColumns;
            });
        }
    }, [isBoardClosed]);

    const addColumn = useCallback(async () => {
        if (isBoardClosed) return;

        const lastColumn = columns[columns.length - 1];
        const newPosition = lastColumn ? lastColumn.position + 1000 : 1000;

        try {
            const result = await createNewColumn(Number(boardId), 'New Column', newPosition)
            notify.success(result.message);
            await fetchBoardData();
        } catch (error: any) {
            notify.error(error.response?.data?.message);
        }
    }, [isBoardClosed, columns, boardId]);

    const handleUpdateColumnTitle = useCallback(async (columnId: number, newTitle: string) => {
        if (isBoardClosed) return;
        await updateColumn(Number(boardId), columnId, newTitle)
            .then(data => {
                console.log(isBoardClosed, activeElements.activeColumn);
                notify.success(data.message)
            })
            .catch(err => notify.success(err.response?.data?.message))
    }, [isBoardClosed]);

    const deleteColumn = useCallback(
        (columnId: number) => {
            if (isBoardClosed) return;

            setColumns(columns =>
                columns.filter(column => column.id !== columnId)
            );
            if (activeInputColumnId === columnId) {
                setActiveInputColumnId(null);
                setCardTitle('');
            }
        },
        [activeInputColumnId, isBoardClosed]
    );

    const handleStartAddingCard = useCallback((columnId: number) => {
        if (isBoardClosed) return;

        setActiveInputColumnId(columnId);
        setCardTitle('');
    }, [isBoardClosed]);

    const handleSubmitCard = useCallback(
        async (columnId: number) => {
            console.log(columnId);
            if (isBoardClosed) return;

            const column = columns.find(col => col.id === columnId);
            if (!column) return;
            const lastTask = column.tasks[column.tasks.length - 1];
            const newPosition = lastTask ? lastTask.position + 1000 : 1000;

            try {
                const result = await taskService.createTask(columnId, cardTitle, newPosition)
                notify.success(result.message);
                await fetchBoardData();
            } catch (error: any) {
                notify.error(error.response?.data?.message);
            }

            setActiveInputColumnId(null);
            setCardTitle('');
            // if (cardTitle.trim()) {
            //     const newItem: Item = {
            //         id: `item-${Date.now()}`,
            //         content: cardTitle.trim(),
            //     };

            //     setColumns(columns =>
            //         columns.map(column =>
            //             column.id === columnId
            //                 ? { ...column, items: [...column.items, newItem] }
            //                 : column
            //         )
            //     );
            // }
        },
        [cardTitle, isBoardClosed]
    );

    const handleCancelCard = useCallback(() => {
        setActiveInputColumnId(null);
        setCardTitle('');
    }, []);

    const deleteItem = useCallback((itemId: number) => {
        if (isBoardClosed) return;

        setColumns(columns =>
            columns.map(column => ({
                ...column,
                items: column.tasks.filter(item => item.id !== itemId),
            }))
        );
    }, [isBoardClosed]);

    const handleArchiveColumn = useCallback(async (columnId: number) => {
        if (isBoardClosed) return;
        try {
            const result = await archiveColumn(columnId);
            notify.success(result.message);
            await fetchBoardData();
        } catch (error: any) {
            notify.error(error.response?.data?.message);
        }
    }, [isBoardClosed]);

    const handleArchiveAllItemInColumns = useCallback((columnId: number) => {
        if (isBoardClosed) return;
        console.log("archived all items", columnId);
    }, [isBoardClosed]);

    const handleHideDetailModal = useCallback(() => {
        setShowDetailModal(false);
    }, []);

    const handleShowDetailModal = useCallback(() => {
        setShowDetailModal(true);
    }, []);

    const handleReopenBoard = useCallback(async () => {
        setIsLoading(true);
        try {
            const result = await reopenBoard(Number(boardId));
            notify.success(result.message);
            await fetchBoardData();
            setIsLoading(false);
        } catch (error: any) {
            notify.error(error.response?.data?.message);
        }
    }, []);

    // OPTIMIZATION: Memoize column props to prevent unnecessary rerenders
    const columnProps = useMemo(
        () =>
            columns.map(col => ({
                key: col.id,
                column: col,
                items: col.tasks,
                isAddingCard: activeInputColumnId === col.id,
                isDragging: isDragging && dragType === 'item',
                isBoardClosed: isBoardClosed,
            })),
        [columns, activeInputColumnId, isDragging, dragType, isBoardClosed]
    );

    return (
        <div className='bg-[#283449] w-full h-full flex flex-col'>
            {
                isLoading ?
                    <div className="mt-6">
                        <LoadingContent />
                    </div> :
                    <>
                        {/* Board Closed Banner */}
                        {boardDetail?.status === 'completed' && (
                            <div className='bg-red-500 text-white px-4 py-3 flex items-center justify-between'>
                                <div className='flex items-center gap-2'>
                                    <Lock size={18} />
                                    <span className='font-medium'>
                                        This board is closed. Reopen the board to make changes.
                                    </span>
                                </div>
                                <button
                                    onClick={handleReopenBoard}
                                    className='bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md flex items-center gap-2 transition-colors font-medium'
                                >
                                    <Unlock size={16} />
                                    Reopen board
                                </button>
                            </div>
                        )}
                        <BoardNavbar id={boardDetail.id} name={boardDetail?.name} isBoardClosed={isBoardClosed} />

                        <div className={`grow overflow-hidden ${isBoardClosed ? 'pointer-events-none opacity-60' : ''}`}>
                            <DndContext
                                sensors={sensors}
                                collisionDetection={pointerWithin}
                                onDragStart={handleDragStart}
                                onDragOver={handleDragOver}
                                onDragEnd={handleDragEnd}
                            >
                                <div className='h-full flex items-start overflow-x-auto gap-4 p-4'>
                                    <SortableContext
                                        items={columnIds}
                                        strategy={horizontalListSortingStrategy}
                                    >
                                        {columnProps.map(col => (
                                            <DroppableColumn
                                                key={col.key}
                                                column={col.column}
                                                items={col.items}
                                                isAddingCard={col.isAddingCard}
                                                cardTitle={cardTitle}
                                                setCardTitle={setCardTitle}
                                                onStartAddingCard={handleStartAddingCard}
                                                onSubmitCard={handleSubmitCard}
                                                onCancelCard={handleCancelCard}
                                                onDeleteItem={deleteItem}
                                                onDeleteColumn={deleteColumn}
                                                onUpdateColumnTitle={handleUpdateColumnTitle}
                                                onArchiveColumn={handleArchiveColumn}
                                                onArchiveAllItems={handleArchiveAllItemInColumns}
                                                handleShowDetailTask={handleShowDetailModal}
                                            />
                                        ))}
                                    </SortableContext>

                                    <button
                                        onClick={addColumn}
                                        className={`bg-[#ffffff3d] hover:bg-[#ffffff33] rounded-lg p-4 w-80 flex-shrink-0 transition-colors flex items-center justify-center gap-2 text-white ${isBoardClosed ? 'cursor-not-allowed' : ''}`}
                                        disabled={isBoardClosed}
                                    >
                                        <Plus size={20} />
                                        Add another column
                                    </button>
                                </div>
                                <DragOverlay>
                                    {!isBoardClosed && activeElements.activeColumn ? (
                                        <div className='bg-[rgba(0,0,0,0.7)] rounded-lg p-4 w-80 opacity-95 transform rotate-2 shadow-2xl'>
                                            <div className='flex items-center gap-2 mb-4'>
                                                <GripVertical
                                                    size={16}
                                                    className='text-gray-400'
                                                />
                                                <h3 className='font-semibold text-white'>
                                                    {activeElements.activeColumn.name}
                                                </h3>
                                                <span className='bg-gray-300 text-gray-600 text-xs px-2 py-1 rounded-full'>
                                                    {
                                                        activeElements.activeColumn.tasks
                                                            .length
                                                    }
                                                </span>
                                            </div>
                                        </div>
                                    ) : !isBoardClosed && activeElements.activeItem ? (
                                        <div className='bg-[rgba(0,0,0,0.6)] p-3 rounded-lg shadow-2xl opacity-95 transform rotate-2'>
                                            <span className='text-sm text-white whitespace-pre-wrap break-words'>
                                                {activeElements.activeItem.name}
                                            </span>
                                        </div>
                                    ) : null}
                                </DragOverlay>
                            </DndContext>
                        </div>
                        {
                            showDetailModal &&
                            <TaskDetailModal
                                onClose={handleHideDetailModal}
                                item={{ id: '1', content: 'Hello' }}
                            />
                        }
                    </>
            }
        </div>
    );
};

export default WorkspaceBoard;