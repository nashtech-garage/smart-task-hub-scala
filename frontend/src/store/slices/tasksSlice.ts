import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { TasksState } from "@/types";

const initialState: TasksState = {
  byId: {},
  allIds: [],
};

const tasksSlice = createSlice({
  name: "tasks",
  initialState,
  reducers: {
    setTasks: (state, action: PayloadAction<TasksState>) => {
      return action.payload;
    },
  },
});

export const {
  setTasks,
} = tasksSlice.actions;
export default tasksSlice.reducer;
