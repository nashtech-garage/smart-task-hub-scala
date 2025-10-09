import { createSelector } from "@reduxjs/toolkit";
import type { RootState } from "..";
import { selectAllColumnIds, selectColumnsById } from "./columnsSelector";

const selectTasksById = (state: RootState) => state.tasks.byId;
const selectAllArchivedTaskIds = (state: RootState) => state.archivedTasks.allIds;
const selectArchivedTasksById = (state: RootState) => state.archivedTasks.byId;

export const selectAllTasksInColumns = (columnId: number) =>
  createSelector(
    selectColumnsById,
    selectTasksById,
    (columnsById, tasksById) => {
      const taskIds = columnsById[columnId]?.taskIds ?? [];
      return taskIds.map(id => tasksById[id]).filter(Boolean);
    }
  );

export const selectTasksByColumns = createSelector(
  selectColumnsById,
  selectAllColumnIds,
  selectTasksById,
  (columnsById, allIds, tasksById) =>
    Object.fromEntries(
      allIds.map(id => [
        id,
        (columnsById[id].taskIds ?? []).map(tid => tasksById[tid]).filter(Boolean)
      ])
    )
);

export const selectTaskById = (id: number) =>
  (state: RootState) => state.tasks.byId[id];

/**
 * Selector get max position of task in a column.
 * @param columnId ID of the column
 * @returns Maximum position value among tasks in the specified column, or 0 if no tasks exist.
 */
export const selectMaxTaskPositionByColumn = (columnId: number) =>
  createSelector(
    [(state: RootState) => state.columns.byId, (state: RootState) => state.tasks.byId],
    (columnsById, tasksById) => {
      const column = columnsById[columnId];
      if (!column || column.taskIds.length === 0) return 0;

      const tasksInColumn = column.taskIds
        .map(id => tasksById[id])
        .filter(Boolean);

      const maxPosition = Math.max(...tasksInColumn.map(task => task.position));
      return isFinite(maxPosition) ? maxPosition : 0;
    }
  );


export const selectArchivedTasks = createSelector(
  selectArchivedTasksById,
  selectAllArchivedTaskIds,
  (byId, allIds) => allIds.map(id => byId[id])
);