import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

export interface Column {
  id: number;
  name: string;
  position: number;
  projectId: number;
  taskIds: number[];
}

interface ColumnsState {
  byId: Record<number, Column>;
  allIds: number[];
}

const initialState: ColumnsState = {
  byId: {},
  allIds: [],
};

const columnsSlice = createSlice({
  name: "columns",
  initialState,
  reducers: {
    columnCreated: (state, action: PayloadAction<Column>) => {
      const column = action.payload;
      state.byId[column.id] = column;
      if (!state.allIds.includes(column.id)) {
        state.allIds.push(column.id);
      }
    },
    columnUpdated: (state, action: PayloadAction<Column>) => {
      state.byId[action.payload.id] = action.payload;
    },
    columnDeleted: (state, action: PayloadAction<number>) => {
      const id = action.payload;
      delete state.byId[id];
      state.allIds = state.allIds.filter((cid) => cid !== id);
    }
  },
});

export const {
  columnCreated,
  columnUpdated,
  columnDeleted,
} = columnsSlice.actions;
export default columnsSlice.reducer;
