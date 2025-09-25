import type { Column, ColumnsState } from "@/types";
import { normalizeColumns } from "@/utils/normalize";
import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

const initialState: ColumnsState = {
  byId: {},
  allIds: [],
};

const archivedColumnsSlice = createSlice({
  name: "archivedColumns",
  initialState,
  reducers: {
    setArchivedColumns: (state, action: PayloadAction<Column[]>) => {
      return normalizeColumns(action.payload);
    },
    archivedColumnRestored: (state, action: PayloadAction<number>) => {
      const id = action.payload;
      delete state.byId[id];
      state.allIds = state.allIds.filter(cid => cid !== id);
    },
    columnArchived: (state, action: PayloadAction<Column>) => {
      const col = action.payload;
      state.byId[col.id] = col;
      if (!state.allIds.includes(col.id)) {
        state.allIds.push(col.id);
      }
    },
    columnDeleted: (state, action: PayloadAction<number>) => {
      const id = action.payload;
      delete state.byId[id];
      state.allIds = state.allIds.filter(cid => cid !== id);
    }
  },
});

export const { setArchivedColumns, archivedColumnRestored, columnArchived, columnDeleted } =
  archivedColumnsSlice.actions;
export default archivedColumnsSlice.reducer;
