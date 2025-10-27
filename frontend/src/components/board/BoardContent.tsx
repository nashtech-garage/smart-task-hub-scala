import BoardClosedBanner from '@/components/board/BoardClosedBanner';
import BoardNavbar from '@/components/board/BoardNavbar';
import DroppableColumn from '@/components/board/DroppableColumn';
import LoadingContent from '@/components/ui/LoadingContent';
import { useBoardData } from '@/hooks/useBoardData';
import { useBoardOperations } from '@/hooks/useBoardOperations';
import { updateColumnPosititon } from '@/services/boardService';
import taskService from '@/services/taskService';
import { useAppSelector } from '@/store';
import { selectActiveColumns } from '@/store/selectors/columnsSelector';
import type { Column, Task } from '@/types';
import {
    DndContext,
    DragOverlay,
    KeyboardSensor,
    PointerSensor,
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
import { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';

const WorkspaceBoard = () => {
    const { boardId } = useParams();
    console.log("Rendering BoardContent: ", boardId);

    const columns = useAppSelector(selectActiveColumns); // Column[]
    const tasks = useAppSelector(state => state.tasks);
    const {
        boardDetail,
        isLoading,
        isBoardClosed,
        setIsBoardClosed,
        reopenBoard,
    } = useBoardData(Number(boardId));

    const { addColumn } = useBoardOperations({
        boardId: Number(boardId),
        isBoardClosed,
        columns,
    });

    const [dragData, setDragData] = useState<
        {
            id: UniqueIdentifier | null;
            type: 'column' | 'item' | null;
            data: Column | Task | null;
        }
        | null
    >();

    const [dragLoading, setDragLoading] = useState(false);
    const [localColumns, setLocalColumns] = useState<Column[]>(columns);

    useEffect(() => {
        setLocalColumns(columns);
    }, [columns]);

    const sensors = useSensors(
        useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
        useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
    );

    // columnIds memo to avoid unnecessary SortableContext re-renders
    const columnIds = useMemo(() => localColumns.map((c) => c.id), [localColumns]);

    // ---------- Handlers ----------
    const handleDragStart = useCallback(
        (event: DragStartEvent) => {
            if (isBoardClosed) return;

            console.log("drag start ", event);

            setDragData({
                id: event.active.id,
                type: event.active.data?.current?.type as 'column' | 'item',
                data: event.active.data?.current?.data
            });

            console.log("end of drag data")
        },
        [isBoardClosed]
    );

    const handleDragOver = useCallback(
        (event: DragOverEvent) => {
            if (isBoardClosed) return;
            const { active, over } = event;
            if (!over || over.id === active.id) return;

            const activeType = active.data.current?.type;
            const overType = over.data.current?.type;
            if (activeType !== "item") return;
            console.log("drag over ", event);

            const activeId = Number(active.id);
            const overId = Number(over.id);

            setLocalColumns(prev => {
                const newColumns = structuredClone(prev);

                const fromCol = newColumns.find(c => c.taskIds.includes(activeId));
                let toCol: typeof fromCol | undefined;

                if (overType === "column") {
                    toCol = newColumns.find(c => c.id === overId);
                    if (!toCol) return prev;
                } else {
                    toCol = newColumns.find(c => c.taskIds.includes(overId));
                    if (!toCol) return prev;
                }

                if (!fromCol || !toCol) return prev;

                // reorder in the same column
                if (fromCol.id === toCol.id) {
                    const from = newColumns.find(c => c.id === fromCol.id)!;
                    const oldIndex = from.taskIds.indexOf(activeId);
                    const newIndex =
                        overType === "item"
                            ? from.taskIds.indexOf(overId)
                            : oldIndex;

                    if (oldIndex !== newIndex) {
                        from.taskIds = arrayMove(from.taskIds, oldIndex, newIndex);
                    }
                    return newColumns;
                }

                const from = newColumns.find(c => c.id === fromCol.id)!;
                const to = newColumns.find(c => c.id === toCol.id)!;

                // remove task from the original column
                from.taskIds = from.taskIds.filter(id => id !== activeId);
                from.totalTasks = from.totalTasks - 1;

                // if over is empty column, insert task to it
                // otherwise return 
                if (overType === "column") {
                    if (to.totalTasks > 0) return prev;
                    to.taskIds.push(activeId);
                    to.totalTasks = to.totalTasks + 1;
                    return newColumns;
                }

                const overIndex = to.taskIds.indexOf(overId);
                to.taskIds.splice(overIndex, 0, activeId);
                to.totalTasks = to.totalTasks + 1;

                return newColumns;
            });
        },
        [isBoardClosed, localColumns]
    );


    const handleDragEnd = async (event: DragEndEvent) => {
        const { active, over } = event;
        console.log("drag end ", event);
        setDragData(null);

        if (!over || isBoardClosed) return;

        const activeIdNum = Number(active.id);
        const overIdNum = Number(over.id);

        const activeIsColumn = active.data?.current?.type === 'column';
        const overIsColumn = over.data?.current?.type === 'column';

        // COLUMN REORDER
        if (activeIsColumn && overIsColumn && activeIdNum !== overIdNum) {
            const oldIndex = columns.findIndex(c => c.id === activeIdNum);
            const newIndex = columns.findIndex(c => c.id === overIdNum);
            const newColumns = arrayMove(columns, oldIndex, newIndex);

            const movedIndex = newIndex;
            const preCol = newColumns[movedIndex - 1] ?? null;
            const nextCol = newColumns[movedIndex + 1] ?? null;

            let newPosition: number;
            if (preCol && nextCol) newPosition = Math.floor((preCol.position + nextCol.position) / 2);
            else if (!preCol && nextCol) newPosition = Math.floor(nextCol.position / 2);
            else if (preCol && !nextCol) newPosition = preCol.position + 1000;
            else newPosition = 1000;

            try {
                // setDragLoading(true);
                await updateColumnPosititon(Number(boardId), activeIdNum, newPosition);
            } catch (err) {
                console.error('updateColumnPosititon failed', err);
            } finally {
                setDragLoading(false);
            }
        }
        // TASK REORDER
        else {
            const targetCol = localColumns.find(c => c.taskIds.includes(overIdNum));
            if (!targetCol) return;
            const targetIndex = targetCol.taskIds.indexOf(activeIdNum);
            if (targetIndex === -1) return
            const prevTaskId = targetCol.taskIds[targetIndex - 1] ?? null;
            const nextTaskId = targetCol.taskIds[targetIndex + 1] ?? null;

            let newPosition: number;
            if (prevTaskId && nextTaskId) newPosition = Math.floor((tasks.byId[prevTaskId].position + tasks.byId[nextTaskId].position) / 2);
            else if (!prevTaskId && nextTaskId) newPosition = Math.floor(tasks.byId[nextTaskId].position / 2);
            else if (prevTaskId && !nextTaskId) newPosition = tasks.byId[prevTaskId].position + 1000;
            else newPosition = 1000;

            try {
                // setDragLoading(true);
                await taskService.updateTaskPosition(activeIdNum, targetCol.id, newPosition);
            } catch (err) {
                console.error('updateColumnPosititon failed', err);
            } finally {
                setDragLoading(false);
            }
        }

    };


    // ---------- Render ----------
    return (
        <div className="bg-[#283449] w-full h-full flex flex-col">
            {isLoading ? (
                <div className="mt-6">
                    <LoadingContent />
                </div>
            ) : (
                <>
                    <BoardClosedBanner
                        isBoardClosed={isBoardClosed}
                        handleReopenBoard={reopenBoard}
                    />
                    <BoardNavbar
                        id={boardDetail.id}
                        name={boardDetail?.name}
                        isBoardClosed={isBoardClosed}
                        setIsBoardClosed={setIsBoardClosed}
                    />

                    <div className={`grow overflow-hidden ${isBoardClosed ? 'pointer-events-none opacity-60' : ''}`}>
                        <DndContext
                            sensors={sensors}
                            // collisionDetection={pointerWithin}
                            onDragStart={handleDragStart}
                            onDragOver={handleDragOver}
                            onDragEnd={handleDragEnd}
                        >
                            <div className="h-full flex items-start overflow-x-auto gap-4 p-4">
                                <SortableContext items={columnIds} strategy={horizontalListSortingStrategy}>
                                    {localColumns.map((col) => (
                                        <DroppableColumn key={col.id} column={col} itemIds={col.taskIds} boardId={Number(boardId)} />
                                    ))}
                                </SortableContext>

                                {!isBoardClosed && (
                                    <button
                                        onClick={addColumn}
                                        className={`bg-[#ffffff3d] hover:bg-[#ffffff33] rounded-lg p-4 w-80 flex-shrink-0 transition-colors flex items-center justify-center gap-2 text-white ${isBoardClosed ? 'cursor-not-allowed' : ''}`}
                                        disabled={isBoardClosed}
                                    >
                                        <Plus size={20} />
                                        Add another column
                                    </button>
                                )}
                            </div>

                            {/* Drag overlay uses local activeData to avoid disappearing during redux rerender */}
                            <DragOverlay>
                                {!isBoardClosed && dragData?.type === 'column' ? (
                                    <div className="bg-[rgba(0,0,0,0.7)] rounded-lg p-4 w-80 opacity-95 transform rotate-2 shadow-2xl">
                                        <div className="flex items-center gap-2 mb-4">
                                            <GripVertical size={16} className="text-gray-400" />
                                            <h3 className="font-semibold text-white">{(dragData.data as Column).name}</h3>
                                            <span className="bg-gray-300 text-gray-600 text-xs px-2 py-1 rounded-full">
                                                {(dragData.data as Column).totalTasks ?? 0}
                                            </span>
                                        </div>
                                    </div>
                                ) : !isBoardClosed && dragData?.type === 'item' ? (
                                    <div className="bg-[rgba(0,0,0,0.6)] p-3 rounded-lg shadow-2xl opacity-95 transform rotate-2">
                                        <span className="text-sm text-white whitespace-pre-wrap break-words">
                                            {(dragData.data as Task).name}
                                        </span>
                                    </div>
                                ) : null}
                            </DragOverlay>
                        </DndContext>
                    </div>
                    {dragLoading && (
                        <div className="absolute inset-0 flex items-center justify-center bg-white/40 z-20">
                            <div className="w-10 h-10 border-4 border-gray-300 border-t-blue-500 rounded-full animate-spin"></div>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default memo(WorkspaceBoard);
