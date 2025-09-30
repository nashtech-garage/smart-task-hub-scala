import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { Column, ColumnsState } from "@/types";
import { normalizeColumns } from "@/utils/normalize";

const initialState: ColumnsState = {
  byId: {},
  allIds: [],
};

const columnsSlice = createSlice({
  name: "columns",
  initialState,
  reducers: {
    setColumns: (state, action: PayloadAction<Column[]>) => {
      return normalizeColumns(action.payload);
    },
    columnCreated: (state, action: PayloadAction<Column>) => {
      const column = action.payload;
      state.byId[column.id] = column;
      state.allIds.push(column.id);
    },
    columnUpdated: (state, action: PayloadAction<{ columnId: number; name: string }>) => {
      const { columnId, name } = action.payload;

      const column = state.byId[columnId];
      if (column) {
          column.name = name;
      }
    },
    columnRemoved: (state, action: PayloadAction<number>) => {
      const id = action.payload;
      delete state.byId[id];
      state.allIds = state.allIds.filter((cid) => cid !== id);
    },
    columnRestored: (state, action: PayloadAction<Column>) => {
      const column = action.payload;
      state.byId[column.id] = column;

      const index = state.allIds.findIndex(id => state.byId[id].position > column.position);
      if (index === -1) {
        state.allIds.push(column.id);
      } else {
        state.allIds.splice(index, 0, column.id);
      }
    },
    columnsReordered: (state, action: PayloadAction<Column[]>) => {
      action.payload.forEach(col => {
        state.byId[col.id] = col;
      });
      state.allIds = action.payload.map(col => col.id);
    },
    columnReplaced: (
      state,
      action: PayloadAction<{ tempId: number; realColumn: Column }>
    ) => {
      const { tempId, realColumn } = action.payload;
      // remove temp
      delete state.byId[tempId];
      state.allIds = state.allIds.filter(id => id !== tempId);

      // add real
      state.byId[realColumn.id] = realColumn;
      state.allIds.push(realColumn.id);
    },
    removeTaskFromColumn: (state, action: PayloadAction<number>) => {
      const itemId = action.payload;
      state.allIds = state.allIds.filter((tid) => tid !== itemId);

      Object.values(state.byId).forEach((column) => {
        column.taskIds = column.taskIds.filter((id) => id !== itemId);
      });
    },
    addTaskToColumn: (state, action: PayloadAction<{ columnId: number; taskId: number, index: number }>) => {
      const { columnId, taskId, index } = action.payload;
      const column = state.byId[columnId];
      if (column) {
        index === -1 ? column.taskIds.push(taskId) : column.taskIds.splice(index, 0, taskId);
      }
    },
  },
});

export const {
  setColumns,
  columnCreated,
  columnUpdated,
  columnRemoved,
  removeTaskFromColumn,
  columnsReordered,
  columnReplaced,
  columnRestored,
  addTaskToColumn
} = columnsSlice.actions;
export default columnsSlice.reducer;
