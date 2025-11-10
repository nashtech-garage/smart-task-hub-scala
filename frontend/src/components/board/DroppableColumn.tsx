import type { Column } from '@/types';
import {
    SortableContext,
    useSortable,
    verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import React, {
    useEffect,
    useRef,
} from 'react';
import DraggableItem from './DraggableItem';
import ColumnHeader from './ColumnHeader';
import AddTask from './AddTask';
import type { UniqueIdentifier } from '@dnd-kit/core';
import { useInfiniteQuery } from '@tanstack/react-query'
import { useVirtualizer } from "@tanstack/react-virtual";
import taskService from '@/services/taskService';
import { useDispatch } from 'react-redux';
import { appendTaskListToColumn } from '@/store/slices/columnsSlice';
import { appendTaskList } from '@/store/slices/tasksSlice';

interface DroppableColumnProps {
    boardId: number;
    column: Column;
    itemIds: UniqueIdentifier[];
}

const DroppableColumnComponent: React.FC<DroppableColumnProps> = ({
    boardId,
    column,
    itemIds,
}) => {
    const columnRef = useRef<HTMLDivElement>(null);
    const limit = 20;
    const dispatch = useDispatch();

    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
        isDragging,
    } = useSortable({
        id: column.id,
        data: {
            type: 'column',
            data: column,
        },
    });

    const fetchTasksByColumn = async ({ pageParam = 1 }) => {
        const res = await taskService.getTaskByColumnId(boardId, column.id, {
            page: pageParam,
            limit,
        });
        const taskList = res.data.filter(t => !column.taskIds.includes(t.id));
        if (taskList.length === 0) return [];
        dispatch(appendTaskListToColumn({ columnId: column.id, taskIds: taskList.map(t => t.id) }))
        dispatch(appendTaskList(taskList));
        return taskList;
    };

    const {
        data,
        fetchNextPage,
        hasNextPage,
        isFetchingNextPage,
    } = useInfiniteQuery({
        queryKey: ['tasks', column.id],
        queryFn: fetchTasksByColumn,
        initialPageParam: 1,
        getNextPageParam: (lastPage, allPages, lastPageParam) => {
            const loadedTasks = allPages.flatMap(p => p).length;
            // if (lastPage.length < limit) return undefined;
            return loadedTasks < column.totalTasks ? lastPageParam + 1 : undefined;
        },
        enabled: column.totalTasks > limit
    });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
    };

    // === Virtualization setup ===
    const scrollParentRef = useRef<HTMLDivElement>(null);

    const virtualizer = useVirtualizer({
        count: itemIds.length,
        getScrollElement: () => scrollParentRef.current,
        estimateSize: () => 80, // avarage height in pixel
        overscan: 5, // overscan count
    });
    const virtualItems = virtualizer.getVirtualItems()

    useEffect(() => {
        virtualizer.measure();
    }, [itemIds]);

    useEffect(() => {
        if (column.totalTasks <= limit) return;
        const lastItem = virtualItems[virtualItems.length - 1]
        if (
            !hasNextPage ||
            isFetchingNextPage ||
            !lastItem ||
            lastItem.index < itemIds.length - 6
        )
            return

        fetchNextPage();
    }, [virtualItems, hasNextPage, isFetchingNextPage, column, fetchNextPage])

    console.log('Rendering DroppableColumn:', column.id, column.name);

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={`select-none bg-[var(--column-bg)] rounded-xl p-2 pr-1 w-[272px] flex-shrink-0 
                max-h-full flex flex-col
                ${isDragging ? 'opacity-50' : ''}`}
        >

            <ColumnHeader
                columnRef={columnRef}
                listeners={listeners}
                attributes={attributes}
                column={column}
            />
            <SortableContext
                items={itemIds}
                strategy={verticalListSortingStrategy}
            >
                <div
                    ref={scrollParentRef}
                    className='overflow-y-auto space-y-3 pr-1'
                >
                    <div
                        style={{
                            height: virtualizer.getTotalSize(),
                            position: "relative",
                        }}
                        className="space-y-3 pr-1"
                    >
                        <div
                            style={{
                                position: "absolute",
                                top: 0,
                                left: 0,
                                width: "100%",
                                transform: `translateY(${virtualizer.getVirtualItems()[0]?.start ?? 0}px)`,
                            }}
                        >
                            {virtualizer.getVirtualItems().map((virtualRow) => {
                                const taskId = itemIds[virtualRow.index];
                                return (
                                    <div
                                        className='my-2'
                                        key={taskId}
                                        data-index={virtualRow.index}
                                        ref={virtualizer.measureElement}
                                    >
                                        <DraggableItem itemId={Number(taskId)} />
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>
            </SortableContext>

            <AddTask columnId={column.id} />
        </div>
    );
};

// Custom comparison function for React.memo
const arePropsEqual = (
    prevProps: DroppableColumnProps,
    nextProps: DroppableColumnProps
) => {
    // Quick checks for primitive values
    if (
        prevProps.column.id !== nextProps.column.id ||
        prevProps.column.name !== nextProps.column.name
    ) {
        return false;
    }

    // Check items array
    if (prevProps.itemIds.length !== nextProps.itemIds.length) {
        return false;
    }

    // Compare each item
    for (let i = 0; i < prevProps.itemIds.length; i++) {
        const prevItem = prevProps.itemIds[i];
        const nextItem = nextProps.itemIds[i];

        if (
            prevItem !== nextItem
            // prevItem.name !== nextItem.name
        ) {
            return false;
        }
    }

    // If all checks pass, props are equal
    return true;
};

const DroppableColumn = React.memo(DroppableColumnComponent, arePropsEqual);

DroppableColumn.displayName = 'DroppableColumn';

export default DroppableColumn;