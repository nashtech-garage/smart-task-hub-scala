import { createSelector } from "@reduxjs/toolkit";
import type { RootState } from "..";
import { selectAllColumnIds, selectColumnsById } from "./columnsSelector";

const selectTasksById = (state: RootState) => state.columns.byId;
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

export const selectArchivedTasks = createSelector(
  selectArchivedTasksById,
  selectAllArchivedTaskIds,
  (byId, allIds) => allIds.map(id => byId[id])
);