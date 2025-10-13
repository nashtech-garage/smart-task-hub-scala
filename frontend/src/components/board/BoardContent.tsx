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
import { selectTasksByColumns } from '@/store/selectors/tasksSelectors';
import { addTaskToColumn, removeTaskFromColumn, setColumns } from '@/store/slices/columnsSlice';
import { taskReordered } from '@/store/slices/tasksSlice';
import type { Column, Task } from '@/types';
import {
  DndContext,
  DragOverlay,
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
import { memo, useCallback, useMemo, useState } from 'react';
import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

const WorkspaceBoard = () => {
  const { boardId } = useParams();
  const dispatch = useDispatch();

  const columns = useAppSelector(selectActiveColumns); // Column[]
  const tasks = useAppSelector(state => state.tasks);
  const {
    boardDetail,
    isLoading,
    isBoardClosed,
    setIsBoardClosed,
    reopenBoard,
    setIsLoading
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

  // const [overData, setOverData] = useRef<Task>(null);

  console.log("Rendering BoardContent: ", boardId);
  console.log("DragData: ", dragData)

  // Drag state for overlay & quick lookup (local, detached from Redux)


  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  // columnIds memo to avoid unnecessary SortableContext re-renders
  const columnIds = useMemo(() => columns.map((c) => c.id), [columns]);

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
      if (!over) return;
      if (active.id === over.id && active.data?.current?.type === over.data?.current?.type) return;

      console.log("drag over ", event)

      // If dragging columns (reordering columns), we don't need onDragOver for visual move because sortable handles it.

      // If dragging an item, and over is a column or item in another column -> move item optimistically
      if (active.data?.current?.type === 'item') {
        const activeItemId = Number(active.id);
        const overIdNum = Number(over.id);
        const fromColumn = columns.find((c) => c.taskIds.includes(activeItemId));

        if (!fromColumn) return;

        if (over.data?.current?.type === 'item') {
          // If still same column and over item same, do nothing
          const toColumn = columns.find((c) => c.taskIds.includes(overIdNum));
          if (!toColumn || fromColumn.id === toColumn.id) return;

          // Move item from source column -> target column
          console.log("drag item in different column")
          const toIndex = toColumn.taskIds.indexOf(overIdNum);
          const prevItem = tasks.byId[toColumn.taskIds[toIndex - 1]] ?? null;
          const nextItem = tasks.byId[toColumn.taskIds[toIndex]] ?? null;

          let newPosition: number;
          if (prevItem && nextItem) {
            newPosition = Math.floor((prevItem.position + nextItem.position) / 2);
          } else if (!prevItem && nextItem) {
            newPosition = Math.floor(nextItem.position / 2);
          } else if (prevItem && !nextItem) {
            newPosition = prevItem.position + 1000;
          } else {
            newPosition = 1000;
          }

          dispatch(removeTaskFromColumn({ taskId: activeItemId, columnId: fromColumn.id }));
          dispatch(taskReordered({ taskId: activeItemId, newPosition }));
          dispatch(addTaskToColumn({ columnId: toColumn.id, taskId: activeItemId, index: toIndex }))
        }
        // drag item into empty column
        else if (over.data?.current?.type === 'column' && fromColumn.id !== overIdNum && over.data.current.data.taskIds.length === 0) {
          console.log("drag item into empty column")
          dispatch(removeTaskFromColumn({ taskId: activeItemId, columnId: fromColumn.id }));
          dispatch(addTaskToColumn({
            columnId: Number(over.id),
            taskId: Number(activeItemId),
            index: -1
          }));
        }
      }

      // For column dragging we rely on SortableContext default behavior — no custom onDragOver required
    },
    [isBoardClosed, columns, dispatch]
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

    // ==============================
    // 1️⃣ COLUMN REORDER
    // ==============================
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
        setIsLoading(true);
        await updateColumnPosititon(Number(boardId), activeIdNum, newPosition);
      } catch (err) {
        console.error('updateColumnPosititon failed', err);
      } finally {
        setIsLoading(false);
      }
      return;
    }

    // ==============================
    // 2️⃣ TASK REORDER
    // ==============================
    if (!activeIsColumn && !overIsColumn) {
      const fromCol = columns.find(c => c.taskIds.includes(activeIdNum));
      const toCol = columns.find(c => c.taskIds.includes(overIdNum));
      if (!fromCol || !toCol) return;

      // ===== SAME COLUMN =====
      if (fromCol.id === toCol.id) {
        const tasksInCol = [...fromCol.taskIds];
        const oldIndex = tasksInCol.indexOf(activeIdNum);
        const newIndex = tasksInCol.indexOf(overIdNum);

        // ✅ find direction (before / after)
        const activeRect = active.rect.current.translated;
        const overRect = over.rect;
        const isAfter = activeRect && overRect
          ? activeRect.top > overRect.top + overRect.height / 2
          : false;

        // move task into temp array
        tasksInCol.splice(oldIndex, 1);
        const targetIndex = isAfter ? newIndex + 1 : newIndex;
        tasksInCol.splice(targetIndex, 0, activeIdNum);

        // ✅ find prev/next to calculate position
        const movedIndex = tasksInCol.indexOf(activeIdNum);
        const prevTaskId = tasksInCol[movedIndex - 1];
        const nextTaskId = tasksInCol[movedIndex + 1];

        const prevItem = prevTaskId ? tasks.byId[prevTaskId] : null;
        const nextItem = nextTaskId ? tasks.byId[nextTaskId] : null;

        let newPosition: number;
        if (prevItem && nextItem)
          newPosition = Math.floor((prevItem.position + nextItem.position) / 2);
        else if (!prevItem && nextItem)
          newPosition = Math.floor(nextItem.position / 2);
        else if (prevItem && !nextItem)
          newPosition = prevItem.position + 1000;
        else newPosition = 1000;

        // Persist + update UI
        try {
          setIsLoading(true);
          await taskService.updateTaskPosition(activeIdNum, fromCol.id, newPosition);
          dispatch(taskReordered({ taskId: activeIdNum, newPosition }));
          dispatch(setColumns(
            columns.map(c =>
              c.id === fromCol.id ? { ...c, taskIds: tasksInCol } : c
            )
          ));
        } catch (err) {
          console.error('updateTaskPosititon failed', err);
        } finally {
          setIsLoading(false);
        }
        return;
      }

      // ===== DIFFERENT COLUMN =====
      if (fromCol.id !== toCol.id) {
        const newColumns = columns.map(c => ({ ...c, taskIds: [...c.taskIds] }));
        const from = newColumns.find(c => c.id === fromCol.id)!;
        const to = newColumns.find(c => c.id === toCol.id)!;

        from.taskIds = from.taskIds.filter(id => id !== activeIdNum);

        // ✅ xác định hướng before/after để insert đúng chỗ
        const overIndex = to.taskIds.indexOf(overIdNum);
        const activeRect = active.rect.current.translated;
        const overRect = over.rect;
        const isAfter = activeRect && overRect
          ? activeRect.top > overRect.top + overRect.height / 2
          : false;

        const insertAt = isAfter ? overIndex + 1 : overIndex;
        to.taskIds.splice(insertAt, 0, activeIdNum);

        // ✅ prev/next để tính position chính xác
        const movedIndex = to.taskIds.indexOf(activeIdNum);
        const prevTaskId = to.taskIds[movedIndex - 1];
        const nextTaskId = to.taskIds[movedIndex + 1];
        const prevItem = prevTaskId ? tasks.byId[prevTaskId] : null;
        const nextItem = nextTaskId ? tasks.byId[nextTaskId] : null;

        let newPosition: number;
        if (prevItem && nextItem)
          newPosition = Math.floor((prevItem.position + nextItem.position) / 2);
        else if (!prevItem && nextItem)
          newPosition = Math.floor(nextItem.position / 2);
        else if (prevItem && !nextItem)
          newPosition = prevItem.position + 1000;
        else newPosition = 1000;

        try {
          setIsLoading(true);
          await taskService.updateTaskPosition(activeIdNum, to.id, newPosition);
          dispatch(removeTaskFromColumn({ taskId: activeIdNum, columnId: from.id }));
          dispatch(addTaskToColumn({ columnId: to.id, taskId: activeIdNum, index: insertAt }));
          dispatch(taskReordered({ taskId: activeIdNum, newPosition }));
        } catch (err) {
          console.error('updateTaskPosititon failed', err);
        } finally {
          setIsLoading(false);
        }
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
              collisionDetection={pointerWithin}
              onDragStart={handleDragStart}
              onDragOver={handleDragOver}
              onDragEnd={handleDragEnd}
            >
              <div className="h-full flex items-start overflow-x-auto gap-4 p-4">
                <SortableContext items={columnIds} strategy={horizontalListSortingStrategy}>
                  {columns.map((col) => (
                    <DroppableColumn key={col.id} column={col} itemIds={col.taskIds} />
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
                        {(dragData.data as Column).taskIds?.length ?? 0}
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
        </>
      )}
    </div>
  );
};

export default memo(WorkspaceBoard);
