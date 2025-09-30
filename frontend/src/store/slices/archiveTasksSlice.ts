import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { Item, TasksState } from "@/types";
import { normalizeTasks } from "@/utils/normalize";

const initialState: TasksState = {
  byId: {},
  allIds: [],
};

const archivedTasksSlice = createSlice({
  name: "archivedTasks",
  initialState,
  reducers: {
    setArchivedTasks: (state, action: PayloadAction<Item[]>) => {
      return normalizeTasks(action.payload);
    },
    taskArchived: (state, action: PayloadAction<Item>) => {
      const task = action.payload;
      state.byId[task.id] = task;
      if (!state.allIds.includes(task.id)) {
        state.allIds.push(task.id);
      }
    },
    archivedTaskRestored: (state, action: PayloadAction<number>) => {
      const id = action.payload;
      delete state.byId[id];
      state.allIds = state.allIds.filter((tid) => tid !== id);
    },
    taskDeleted: (state, action: PayloadAction<number>) => {
      const id = action.payload;
      delete state.byId[id];
      state.allIds = state.allIds.filter((tid) => tid !== id);
    }
  },
});

export const { setArchivedTasks, taskArchived, archivedTaskRestored, taskDeleted } =
  archivedTasksSlice.actions;

export default archivedTasksSlice.reducer;