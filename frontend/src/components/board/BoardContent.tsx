import BoardClosedBanner from '@/components/board/BoardClosedBanner';
import BoardNavbar from '@/components/board/BoardNavbar';
import DroppableColumn from '@/components/board/DroppableColumn';
import LoadingContent from '@/components/ui/LoadingContent';
import { useBoardData } from '@/hooks/useBoardData';
import { useBoardOperations } from '@/hooks/useBoardOperations';
import { updateColumnPosititon } from '@/services/boardService';
import { useAppSelector } from '@/store';
import { selectActiveColumns } from '@/store/selectors/columnsSelector';
import { selectTaskById, selectTasksByColumns } from '@/store/selectors/tasksSelectors';
import { columnsReordered, setColumns } from '@/store/slices/columnsSlice';
import type { Column } from '@/types';
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
import { GripVertical, Plus } from 'lucide-react';
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

const WorkspaceBoard = () => {

    const { boardId } = useParams();
    const [activeId, setActiveId] = useState<UniqueIdentifier | null>(null);
    const containerRef = useRef<HTMLDivElement>(null);
    const dispatch = useDispatch();
    const columns = useAppSelector(selectActiveColumns);
    const tasksByColumn = useAppSelector(selectTasksByColumns);

    const {
        boardDetail,
        isLoading,
        isBoardClosed,
        setIsBoardClosed,
        reopenBoard,
    } = useBoardData(Number(boardId));

    const {
        addColumn,
    } = useBoardOperations({
        boardId: Number(boardId),
        isBoardClosed,
        columns,
    });

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
    const activeTask = useAppSelector(
        activeId ? selectTaskById(activeId as number) : () => null
    );

    const activeColumn = useMemo(() => {
        if (!activeId) return null;
        return columns.find(col => col.id === activeId) ?? null;
    }, [activeId, columns]);

    const activeElements = { activeColumn, activeTask };

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
        },
        [columns, isBoardClosed]
    );

    // OPTIMIZATION: Heavily optimized drag over with batching and debouncing
    const handleDragOver = useCallback(
        (event: DragOverEvent) => {
            const { active, over } = event;

            if (!over || !isDragging || isBoardClosed) return;

            const activeId = active.id as number;
            const overId = over.id as number;

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
                        col.taskIds.includes(activeId)
                    );
                    const overColumnIndex = newColumns.findIndex(col =>
                        col.taskIds.includes(overId)
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

                        const activeItemIndex = activeColumn.taskIds.indexOf(activeId);
                        const overItemIndex = overColumn.taskIds.indexOf(overId);

                        if (activeItemIndex !== -1 && overItemIndex !== -1) {
                            activeColumn.taskIds = [...activeColumn.taskIds];
                            overColumn.taskIds = [...overColumn.taskIds];

                            activeColumn.taskIds.splice(activeItemIndex, 1);
                            overColumn.taskIds.splice(overItemIndex, 0, activeId);


                            newColumns[activeColumnIndex] = activeColumn;
                            newColumns[overColumnIndex] = overColumn;
                            hasChanged = true;
                        }
                    }
                }

                // Item over column
                if (isActiveAnItem && isOverAColumn) {
                    const activeColumnIndex = newColumns.findIndex(col =>
                        col.taskIds.includes(activeId)
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

                        const activeItemIndex = activeColumn.taskIds.indexOf(activeId);

                        if (activeItemIndex !== -1) {
                            activeColumn.taskIds = [...activeColumn.taskIds];
                            overColumn.taskIds = [...overColumn.taskIds];

                            activeColumn.taskIds.splice(activeItemIndex, 1);
                            overColumn.taskIds.push(activeId);

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
                    dispatch(columnsReordered(newColumns));
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
            dispatch(setColumns(dragStateRef.current.originalColumns));
            return;
        }

        if (!over) {
            // Restore original state if cancelled
            dispatch(setColumns(dragStateRef.current.originalColumns));
            return;
        }

        const activeId = active.id as number;
        const overId = over.id as number;

        const isActiveAColumn = active.data.current?.type === 'column';
        const isOverAColumn = over.data.current?.type === 'column';
        const isActiveAnItem = active.data.current?.type === 'item';
        const isOverAnItem = over.data.current?.type === 'item';

        // Handle column reordering
        if (isActiveAColumn && isOverAColumn) {
            const activeColumnIndex = columns.findIndex(col => col.id === activeId);
            const overColumnIndex = columns.findIndex(col => col.id === overId);

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

                const movedColumn = { ...newColumns[movedIndex], position: Math.floor(newPosition) };
                newColumns[movedIndex] = movedColumn;
                dispatch(columnsReordered(newColumns));
                updateColumnPosititon(Number(boardId), movedColumn.id, movedColumn.position);
            }
        }

        // Handle item reordering within same column
        if (isActiveAnItem && isOverAnItem) {
            const activeColumnIndex = columns.findIndex(col =>
                col.taskIds.includes(activeId)
            );
            const overColumnIndex = columns.findIndex(col =>
                col.taskIds.includes(overId)
            );

            if (activeColumnIndex === overColumnIndex && activeColumnIndex !== -1) {
                const newColumns = [...columns];
                const column = { ...newColumns[activeColumnIndex] };

                const activeItemIndex = column.taskIds.indexOf(activeId);
                const overItemIndex = column.taskIds.indexOf(overId);

                if (activeItemIndex !== -1 && overItemIndex !== -1) {
                    column.taskIds = arrayMove(column.taskIds, activeItemIndex, overItemIndex);

                    newColumns[activeColumnIndex] = column;

                    dispatch(setColumns(newColumns));
                }
            }
        }
    }, [isBoardClosed]);

    console.log("worspace board: " + boardId);
    return (
        <div className='bg-[#283449] w-full h-full flex flex-col'>
            {
                isLoading ?
                    <div className="mt-6">
                        <LoadingContent />
                    </div> :
                    <>
                        <BoardClosedBanner isBoardClosed={isBoardClosed} handleReopenBoard={reopenBoard} />'
                        <BoardNavbar id={boardDetail.id} name={boardDetail?.name} isBoardClosed={isBoardClosed} setIsBoardClosed={setIsBoardClosed} />

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
                                        {columns.map(col => (
                                            <DroppableColumn
                                                key={col.id}
                                                column={col}
                                                items={tasksByColumn[col.id] || []}
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
                                                        activeElements.activeColumn.taskIds
                                                            .length
                                                    }
                                                </span>
                                            </div>
                                        </div>
                                    ) : !isBoardClosed && activeElements.activeTask ? (
                                        <div className='bg-[rgba(0,0,0,0.6)] p-3 rounded-lg shadow-2xl opacity-95 transform rotate-2'>
                                            <span className='text-sm text-white whitespace-pre-wrap break-words'>
                                                {activeElements.activeTask.name}
                                            </span>
                                        </div>
                                    ) : null}
                                </DragOverlay>
                            </DndContext>
                        </div>
                    </>
            }
        </div>
    );
};

export default memo(WorkspaceBoard);