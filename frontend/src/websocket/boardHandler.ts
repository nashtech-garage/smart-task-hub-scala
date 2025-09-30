// store/wsHandlers/boardHandlers.ts
import type { RootState, AppDispatch } from "@/store";
import { columnArchived, archivedColumnRestored, columnDeleted } from "@/store/slices/archiveColumnsSlice";
import { taskArchived, taskDeleted, archivedTaskRestored } from "@/store/slices/archiveTasksSlice";
import { addTaskToColumn, columnCreated, columnRemoved, columnReplaced, columnRestored, columnUpdated, removeTaskFromColumn } from "@/store/slices/columnsSlice";
import { taskCreated, taskRemoved, taskReplaced, taskRestored, taskUpdated } from "@/store/slices/tasksSlice";

export const handleBoardWSMessage = (
  message: any,
  dispatch: AppDispatch,
  getState: () => RootState
) => {
  switch (message.type) {
    case "COLUMN_MOVED":
      console.log("Column moved", message);
      break;

    case "COLUMN_CREATED": {
      const { columnId, name, position } = message.payload;
      const tempCol = getState().columns.allIds
        .map(id => getState().columns.byId[id])
        .find(c => c.id < 0);

      if (tempCol) {
        dispatch(columnReplaced({
          tempId: tempCol.id,
          realColumn: { id: columnId, name, position, taskIds: [] }
        }));
      } else {
        dispatch(columnCreated({ id: columnId, name, position, taskIds: [] }));
      }
      break;
    }

    case "COLUMN_STATUS_UPDATED": {
      const { columnId, updatedStatus } = message.payload;
      console.log("Column status updated", message);

      if (updatedStatus === "archived") {
        const column = getState().columns.byId[columnId];
        if (column) {
          dispatch(columnRemoved(columnId));
          dispatch(columnArchived(column));
        }
      } else if (updatedStatus === "active") {
        const column = getState().archivedColumns.byId[columnId];
        if (column) {
          dispatch(archivedColumnRestored(columnId));
          dispatch(columnRestored(column));
        }
      } else if (updatedStatus === "deleted") {
        dispatch(columnDeleted(columnId));
      }
      break;
    }

    case "COLUMN_UPDATED": {
      const { columnId, name } = message.payload;
      dispatch(columnUpdated({ columnId, name }));
      break;
    }

    case "TASK_CREATED": {
      const { columnId, name, taskPosition, taskId } = message.payload;
      const tempTask = getState().tasks.allIds
        .map(id => getState().tasks.byId[id])
        .find(c => c.id < 0);
      if (tempTask) {
        dispatch(taskReplaced({
          tempId: tempTask.id,
          realTask: { id: taskId, name, position: taskPosition }
        }));

      } else {
        dispatch(taskCreated({ id: taskId, name, position: taskPosition }));
      }
      dispatch(addTaskToColumn({ columnId, taskId, index: -1 }));
      break;
    }

    case "TASK_UPDATED": {
      console.log("Task updated", message);
      const { taskId, columnId, taskPosition, detail, name } = message.payload;
      dispatch(taskUpdated({ taskId, columnId, taskPosition, detail, name }));
      break;
    }

    case "TASK_STATUS_UPDATED": {
      const { taskId, columnId, taskPosition, name, updatedStatus } = message.payload;
      console.log("Task status updated", message);

      if (updatedStatus === "archived") {
        const task = getState().tasks.byId[taskId];
        if (task) {
          dispatch(removeTaskFromColumn(taskId));
          dispatch(taskRemoved(taskId));
          dispatch(taskArchived(task));
        }
      } else if (updatedStatus === "active") {
        const task = getState().archivedTasks.byId[taskId];
        if (task) {
          dispatch(archivedTaskRestored(taskId));
          dispatch(taskRestored(task));

          const column = getState().columns.byId[columnId];
          const index = column.taskIds.findIndex(id => getState().tasks.byId[id].position > task.position);
          dispatch(addTaskToColumn({ columnId, taskId, index: index === -1 ? -1 : index }));
        }
      } else if (updatedStatus === "deleted") {
        dispatch(taskDeleted(taskId));
      }
      break;
    }

    default:
      console.warn("Unknown WS event", message);
  }
};
