import { useCallback } from 'react';
import { useDispatch } from 'react-redux';
import { createNewColumn, updateColumn, updateColumnPosititon } from '@/services/boardService';
import { notify } from '@/services/toastService';
import { columnDeleted } from '@/store/slices/archiveColumnsSlice';
import { archiveColumnThunk } from '@/store/thunks/columnsThunks';
import type { Column } from '@/types';

interface UseBoardOperationsProps {
  boardId: number;
  isBoardClosed: boolean;
  columns: Column[];
}

interface UseBoardOperationsReturn {
  addColumn: () => Promise<void>;
  updateColumnTitle: (columnId: number, newTitle: string) => Promise<void>;
  updateColumnPosition: (columnId: number, position: number) => Promise<void>;
  deleteColumn: (columnId: number) => void;
  archiveColumn: (column: Column) => Promise<void>;
}

export const useBoardOperations = ({
  boardId,
  isBoardClosed,
  columns,
}: UseBoardOperationsProps): UseBoardOperationsReturn => {
  const dispatch = useDispatch();

  /**
   * Add a new column to the board
   * Position is calculated as last column position + 1000
   */
  const addColumn = useCallback(async () => {
    if (isBoardClosed) return;
    const lastColumn = columns[columns.length - 1];
    const newPosition = lastColumn ? lastColumn.position + 1000 : 1000;

    try {
      const result = await createNewColumn(boardId, 'New Column', newPosition);
      notify.success(result.message);
    } catch (error: any) {
      notify.error(error.response?.data?.message || 'Failed to create column');
    }
  }, [isBoardClosed, columns, boardId]);

  /**
   * Update column title
   */
  const updateColumnTitle = useCallback(
    async (columnId: number, newTitle: string) => {
      if (isBoardClosed) return;
      try {
        const result = await updateColumn(boardId, columnId, newTitle);
        notify.success(result.message);
      } catch (error: any) {
        notify.error(error.response?.data?.message || 'Failed to update column');
      }
    },
    [isBoardClosed, boardId]
  );

  /**
   * Update column position after drag and drop
   */
  const updateColumnPosition = useCallback(
    async (columnId: number, position: number) => {
      if (isBoardClosed) return;
      try {
        await updateColumnPosititon(boardId, columnId, position);
      } catch (error: any) {
        notify.error(error.response?.data?.message || 'Failed to update column position');
      }
    },
    [isBoardClosed, boardId]
  );

  /**
   * Delete column (soft delete - moves to archive)
   */
  const deleteColumn = useCallback(
    (columnId: number) => {
      if (isBoardClosed) return;
      dispatch(columnDeleted(columnId));
    },
    [isBoardClosed, dispatch]
  );

  /**
   * Archive column with all its tasks
   */
  const archiveColumn = useCallback(
    async (column: Column) => {
      if (isBoardClosed) return;
      dispatch(archiveColumnThunk(column) as any);
    },
    [isBoardClosed, dispatch]
  );

  return {
    addColumn,
    updateColumnTitle,
    updateColumnPosition,
    deleteColumn,
    archiveColumn,
  };
};