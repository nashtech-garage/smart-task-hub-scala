import type { Column, Task } from '@/types';
import {
    SortableContext,
    useSortable,
    verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import React, {
    useMemo,
    useRef,
} from 'react';
import DraggableItem from './DraggableItem';
import ColumnHeader from './ColumnHeader';
import AddTask from './AddTask';
import type { UniqueIdentifier } from '@dnd-kit/core';

interface DroppableColumnProps {
    column: Column;
    itemIds: UniqueIdentifier[];
}

const DroppableColumnComponent: React.FC<DroppableColumnProps> = ({
    column,
    itemIds,
}) => {
    const columnRef = useRef<HTMLDivElement>(null);

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

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
    };

    // const itemIds = useMemo(() => items.map(item => item.id), [items]);

    console.log('Rendering DroppableColumn:', column.id, column.name);

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={`select-none bg-[#101204] rounded-xl p-2 pr-1 w-[272px] flex-shrink-0 
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
                <div className='overflow-y-auto space-y-3 pr-1'>
                    <div className="overflow-y-auto space-y-3 pr-1">
                        {itemIds?.map(id => (
                            <DraggableItem
                                key={id}
                                itemId={Number(id)}
                            />
                        ))}
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