import BoardClosedBanner from '@/components/board/BoardClosedBanner';
import BoardNavbar from '@/components/board/BoardNavbar';
import DroppableColumn from '@/components/board/DroppableColumn';
import LoadingContent from '@/components/ui/LoadingContent';
import { useBoardData } from '@/hooks/useBoardData';
import { useBoardOperations } from '@/hooks/useBoardOperations';
import { updateColumnPosititon } from '@/services/boardService';
import { useAppSelector } from '@/store';
import { selectActiveColumns } from '@/store/selectors/columnsSelector';
import { selectTasksByColumns } from '@/store/selectors/tasksSelectors';
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
import { memo, useCallback, useMemo, useState } from 'react';
import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

const WorkspaceBoard = () => {
  const { boardId } = useParams();
  const dispatch = useDispatch();

  const columns = useAppSelector(selectActiveColumns); // Column[]
  const tasksByColumn = useAppSelector(selectTasksByColumns); // Record<number, Task[]>

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

  // Drag state for overlay & quick lookup (local, detached from Redux)
  const [activeId, setActiveId] = useState<UniqueIdentifier | null>(null);
  const [activeType, setActiveType] = useState<'column' | 'item' | null>(null);
  const [activeData, setActiveData] = useState<any>(null); // can be Column or Task

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  // columnIds memo to avoid unnecessary SortableContext re-renders
  const columnIds = useMemo(() => columns.map((c) => c.id), [columns]);

  // helpers
  const findColumnIdByItemId = useCallback(
    (itemId: UniqueIdentifier): number | undefined => {
      // itemId might be a column id (number) or task id (number)
      // Check if any column.id === itemId
      const asNum = Number(itemId);
      if (columns.some((c) => c.id === asNum)) return asNum;
      // Else find which column contains the task id
      const col = columns.find((c) => c.taskIds.includes(asNum));
      return col ? col.id : undefined;
    },
    [columns]
  );

  // ---------- Handlers ----------
  const handleDragStart = useCallback(
    (event: DragStartEvent) => {
      if (isBoardClosed) return;

      setActiveId(event.active.id);

      // Prefer using data.current.type if provided by draggable; otherwise fallback to lookup
      const typeFromData = event.active.data?.current?.type as 'column' | 'item' | undefined;

      if (typeFromData === 'column' || columns.some((c) => c.id === Number(event.active.id))) {
        setActiveType('column');
        const col = columns.find((c) => c.id === Number(event.active.id)) ?? null;
        setActiveData(col);
      } else {
        setActiveType('item');
        // find active task from tasksByColumn
        const asNum = Number(event.active.id);
        let foundTask = null;
        for (const colId of Object.keys(tasksByColumn)) {
          const arr = tasksByColumn[Number(colId)] || [];
          const t = arr.find((it: any) => it.id === asNum);
          if (t) {
            foundTask = t;
            break;
          }
        }
        setActiveData(foundTask);
      }
    },
    [isBoardClosed, columns, tasksByColumn]
  );

  const handleDragOver = useCallback(
    (event: DragOverEvent) => {
      if (isBoardClosed) return;
      const { active, over } = event;
      if (!over) return;

      // If dragging columns (reordering columns), we don't need onDragOver for visual move because sortable handles it.
      const typeFromData = active.data?.current?.type as 'column' | 'item' | undefined;

      // If dragging an item, and over is a column or item in another column -> move item optimistically
      if (typeFromData === 'item' || activeType === 'item') {
        const activeItemId = Number(active.id);
        const overIdNum = Number(over.id);

        const fromColumnIndex = columns.findIndex((c) => c.taskIds.includes(activeItemId));
        const toColumnIndex =
          columns.findIndex((c) => c.taskIds.includes(overIdNum)) !== -1
            ? columns.findIndex((c) => c.taskIds.includes(overIdNum))
            : columns.findIndex((c) => c.id === overIdNum);

        if (fromColumnIndex === -1 || toColumnIndex === -1) return;

        // If still same column and over item same, do nothing
        if (fromColumnIndex === toColumnIndex) return;

        // Move item from source column -> target column (append to end)
        const newColumns = columns.map((c) => ({ ...c, taskIds: [...c.taskIds] }));

        // remove from source
        const fromCol = newColumns[fromColumnIndex];
        const idxInFrom = fromCol.taskIds.indexOf(activeItemId);
        if (idxInFrom !== -1) fromCol.taskIds.splice(idxInFrom, 1);

        // insert into target at end (you can adjust to insert at specific index if over is item)
        const toCol = newColumns[toColumnIndex];
        toCol.taskIds = [...toCol.taskIds, activeItemId];

        // dispatch update (optimistic)
        dispatch(setColumns(newColumns));
      }

      // For column dragging we rely on SortableContext default behavior — no custom onDragOver required
    },
    [isBoardClosed, columns, dispatch, activeType, tasksByColumn]
  );

  const handleDragEnd = useCallback(
    async (event: DragEndEvent) => {
      const { active, over } = event;

      // reset overlay state first so DragOverlay disappears smoothly
      setActiveId(null);
      setActiveType(null);
      setActiveData(null);

      if (!over) {
        // nothing to do (cancelled)
        return;
      }

      if (isBoardClosed) {
        // optionally reload original columns from server or keep as-is
        return;
      }

      const activeIdNum = Number(active.id);
      const overIdNum = Number(over.id);

      const activeIsColumn = columns.some((c) => c.id === activeIdNum);
      const overIsColumn = columns.some((c) => c.id === overIdNum);

      // --- Column reorder ---
      if (activeIsColumn && overIsColumn && activeIdNum !== overIdNum) {
        const oldIndex = columns.findIndex((c) => c.id === activeIdNum);
        const newIndex = columns.findIndex((c) => c.id === overIdNum);
        const newColumns = arrayMove(columns, oldIndex, newIndex);

        // quick optimistic update in redux

        // compute new position for moved column (same logic as your original)
        const movedIndex = newIndex;
        const preCol = newColumns[movedIndex - 1] ?? null;
        const nextCol = newColumns[movedIndex + 1] ?? null;

        let newPosition: number;
        if (preCol && nextCol) {
          newPosition = Math.floor((preCol.position + nextCol.position) / 2);
        } else if (!preCol && nextCol) {
          newPosition = Math.floor(nextCol.position / 2);
        } else if (preCol && !nextCol) {
          newPosition = preCol.position + 1000;
        } else {
          newPosition = 1000;
        }

        dispatch(columnsReordered({ columnId: Number(active.id), newPosition }));
        const movedColumn = { ...newColumns[movedIndex], position: Math.floor(newPosition) };
        // update local array with final pos
        const finalColumns = newColumns.map((c, idx) => (idx === movedIndex ? movedColumn : c));
        dispatch(setColumns(finalColumns));

        // Persist to backend (fire-and-forget; handle errors in service)
        try {
          await updateColumnPosititon(Number(boardId), movedColumn.id, movedColumn.position);
        } catch (err) {
          // error handling: you might want to refetch columns here or rollback
          console.error('updateColumnPosititon failed', err);
        }

        return;
      }

      // --- Task reorder inside same column (or between items in same column) ---
      const activeIsItem = active.data?.current?.type === 'item' || !activeIsColumn;
      const overIsItem = over.data?.current?.type === 'item' || !overIsColumn;

      if (activeIsItem && overIsItem) {
        // find source & target column
        const fromColIndex = columns.findIndex((c) => c.taskIds.includes(activeIdNum));
        const toColIndex =
          columns.findIndex((c) => c.taskIds.includes(overIdNum)) !== -1
            ? columns.findIndex((c) => c.taskIds.includes(overIdNum))
            : columns.findIndex((c) => c.id === overIdNum);

        if (fromColIndex === -1 || toColIndex === -1) return;

        // same column reorder (when both in same column)
        if (fromColIndex === toColIndex) {
          const newColumns = columns.map((c) => ({ ...c, taskIds: [...c.taskIds] }));
          const col = newColumns[fromColIndex];
          const oldIndex = col.taskIds.indexOf(activeIdNum);
          const newIndex = col.taskIds.indexOf(overIdNum);
          if (oldIndex !== -1 && newIndex !== -1 && oldIndex !== newIndex) {
            col.taskIds = arrayMove(col.taskIds, oldIndex, newIndex);
            dispatch(setColumns(newColumns));
          }
        } else {
          // moved across columns - here we already did optimistic move in onDragOver in many cases,
          // but ensure final state is correct: remove from source, insert before/after over item in target
          const newColumns = columns.map((c) => ({ ...c, taskIds: [...c.taskIds] }));

          // remove from source
          const fromCol = newColumns[fromColIndex];
          const removeIndex = fromCol.taskIds.indexOf(activeIdNum);
          if (removeIndex !== -1) fromCol.taskIds.splice(removeIndex, 1);

          // insert into target before 'overId' item (if it exists) otherwise push
          const toCol = newColumns[toColIndex];
          const insertAt = toCol.taskIds.indexOf(overIdNum);
          if (insertAt !== -1) {
            toCol.taskIds.splice(insertAt, 0, activeIdNum);
          } else {
            toCol.taskIds.push(activeIdNum);
          }

          dispatch(setColumns(newColumns));
        }

        // TODO: call API to persist task position if you have one.
      }

      // other cases: ignore
    },
    [columns, dispatch, isBoardClosed, boardId]
  );

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
                    <DroppableColumn key={col.id} column={col} items={tasksByColumn[col.id] || []} />
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

              {/* Drag overlay uses local activeData to avoid disappearing during redux rerender */}
              <DragOverlay>
                {!isBoardClosed && activeType === 'column' && activeData ? (
                  <div className="bg-[rgba(0,0,0,0.7)] rounded-lg p-4 w-80 opacity-95 transform rotate-2 shadow-2xl">
                    <div className="flex items-center gap-2 mb-4">
                      <GripVertical size={16} className="text-gray-400" />
                      <h3 className="font-semibold text-white">{(activeData as Column).name}</h3>
                      <span className="bg-gray-300 text-gray-600 text-xs px-2 py-1 rounded-full">
                        {(activeData as Column).taskIds?.length ?? 0}
                      </span>
                    </div>
                  </div>
                ) : !isBoardClosed && activeType === 'item' && activeData ? (
                  <div className="bg-[rgba(0,0,0,0.6)] p-3 rounded-lg shadow-2xl opacity-95 transform rotate-2">
                    <span className="text-sm text-white whitespace-pre-wrap break-words">
                      {(activeData as any).name}
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
