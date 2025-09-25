import { createSelector } from "@reduxjs/toolkit";
import type { RootState } from "..";

export const selectColumnsById = (state: RootState) => state.columns.byId;
export const selectAllColumnIds = (state: RootState) => state.columns.allIds;
export const selectArchivedColumnsById = (state: RootState) => state.archivedColumns.byId;
export const selectAllArchivedColumnIds = (state: RootState) => state.archivedColumns.allIds;
export const selectTasksById = (state: RootState) => state.tasks.byId;

export const selectActiveColumns = createSelector(
  selectColumnsById,
  selectAllColumnIds,
  (byId, allIds) => allIds.map(id => byId[id])
);

export const selectColumnById = (id: number) => (state: RootState) =>
  state.columns.byId[id];

export const selectArchivedColumns = createSelector(
  selectArchivedColumnsById,
  selectAllArchivedColumnIds,
  (byId, allIds) => allIds.map(id => byId[id])
);