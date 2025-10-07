import { useCallback, useState } from 'react';
import { useDispatch } from 'react-redux';
import taskService from '@/services/taskService';
import { notify } from '@/services/toastService';
import { taskDeleted } from '@/store/slices/archiveTasksSlice';
import type { Column, Task, UpdateItemRequest } from '@/types';

interface UseCardOperationsProps {
  boardId: number;
  isBoardClosed: boolean;
  columns: Column[];
  tasksByColumn: Record<number, Task[]>;
}

interface UseCardOperationsReturn {
  // State
  activeInputColumnId: number | null;
  setActiveInputColumnId: (columnId: number | null) => void;
  cardTitle: string;
  setCardTitle: (title: string) => void;
  
  // Operations
  startAddingCard: (columnId: number) => void;
  submitCard: (columnId: number) => Promise<void>;
  cancelCard: () => void;
  updateTask: (taskId: number, data: UpdateItemRequest) => Promise<void>;
  deleteTask: (taskId: number) => void;
  archiveTask: (taskId: number) => Promise<void>;
  archiveAllTasksInColumn: (columnId: number) => Promise<void>;
}

export const useCardOperations = ({
  boardId,
  isBoardClosed,
  columns,
  tasksByColumn,
}: UseCardOperationsProps): UseCardOperationsReturn => {
  const dispatch = useDispatch();
  
  // State for adding new card
  const [activeInputColumnId, setActiveInputColumnId] = useState<number | null>(null);
  const [cardTitle, setCardTitle] = useState('');

  /**
   * Calculate position for new card
   * Returns max position + 1000, or 1000 if no cards exist
   */
  const calculateNewCardPosition = useCallback(
    (columnId: number): number => {
      const tasksInColumn = tasksByColumn[columnId] || [];
      
      if (tasksInColumn.length === 0) {
        return 1000;
      }

      const taskWithMaxPosition = tasksInColumn.reduce(
        (max, task) => (task.position > max.position ? task : max),
        tasksInColumn[0]
      );

      return taskWithMaxPosition.position + 1000;
    },
    [tasksByColumn]
  );

  /**
   * Start adding a new card to a column
   * Opens the input field for the specified column
   */
  const startAddingCard = useCallback(
    (columnId: number) => {
      if (isBoardClosed) return;
      setActiveInputColumnId(columnId);
      setCardTitle('');
    },
    [isBoardClosed]
  );

  /**
   * Submit and create a new card
   */
  const submitCard = useCallback(
    async (columnId: number) => {
      if (isBoardClosed) return;

      // Validate column exists
      const column = columns.find(col => col.id === columnId);
      if (!column) {
        notify.error('Column not found');
        return;
      }

      const newPosition = calculateNewCardPosition(columnId);

      try {
        const result = await taskService.createTask(
          columnId,
          cardTitle.trim(),
          newPosition
        );
        notify.success(result.message);
        
        // Reset state after successful creation
        setActiveInputColumnId(null);
        setCardTitle('');
      } catch (error: any) {
        notify.error(error.response?.data?.message || 'Failed to create card');
      }
    },
    [cardTitle, isBoardClosed, columns, calculateNewCardPosition]
  );

  /**
   * Cancel adding a new card
   * Closes the input field and clears the title
   */
  const cancelCard = useCallback(() => {
    setActiveInputColumnId(null);
    setCardTitle('');
  }, []);

  /**
   * Update an existing task
   */
  const updateTask = useCallback(
    async (taskId: number, data: UpdateItemRequest) => {
      if (isBoardClosed) return;
      try {
        const result = await taskService.updateTask(taskId, data);
        notify.success(result.message);
      } catch (error: any) {
        notify.error(error.response?.data?.message || 'Failed to update task');
      }
    },
    [isBoardClosed]
  );

  /**
   * Delete a task (soft delete - moves to archive)
   */
  const deleteTask = useCallback(
    (taskId: number) => {
      if (isBoardClosed) return;
      dispatch(taskDeleted(taskId));
      
      // If the deleted task's column had active input, close it
      if (activeInputColumnId) {
        const column = columns.find(col => col.id === activeInputColumnId);
        if (column?.taskIds.includes(taskId)) {
          setActiveInputColumnId(null);
          setCardTitle('');
        }
      }
    },
    [isBoardClosed, activeInputColumnId, columns, dispatch]
  );

  /**
   * Archive a single task
   */
  const archiveTask = useCallback(
    async (taskId: number) => {
      if (isBoardClosed) return;
      try {
        const result = await taskService.archiveTask(taskId);
        notify.success(result.message);
      } catch (error: any) {
        notify.error(error.response?.data?.message || 'Failed to archive task');
      }
    },
    [isBoardClosed]
  );

  /**
   * Archive all tasks in a column
   */
  const archiveAllTasksInColumn = useCallback(
    async (columnId: number) => {
      if (isBoardClosed) return;
      const tasksInColumn = tasksByColumn[columnId] || [];
      try {
        // Archive all tasks in parallel
        await Promise.all(
          tasksInColumn.map(task => taskService.archiveTask(task.id))
        );
        
        notify.success(`Archived ${tasksInColumn.length} task(s) successfully`);
      } catch (error: any) {
        notify.error(error.response?.data?.message || 'Failed to archive tasks');
      }
    },
    [isBoardClosed, tasksByColumn]
  );

  return {
    // State
    activeInputColumnId,
    setActiveInputColumnId,
    cardTitle,
    setCardTitle,
    
    // Operations
    startAddingCard,
    submitCard,
    cancelCard,
    updateTask,
    deleteTask,
    archiveTask,
    archiveAllTasksInColumn,
  };
};