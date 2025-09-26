import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { Task, TasksState } from "@/types";

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
    taskCreated: (state, action: PayloadAction<Task>) => {
      const task = action.payload;
      state.byId[task.id] = task;
      state.allIds.push(task.id);
    },
    taskReplaced: (
      state,
      action: PayloadAction<{ tempId: number; realTask: Task }>
    ) => {
      const { tempId, realTask } = action.payload;
      // remove temp
      delete state.byId[tempId];
      state.allIds = state.allIds.filter(id => id !== tempId);

      // add real
      state.byId[realTask.id] = realTask;
      state.allIds.push(realTask.id);
    }
  },
});

export const {
  setTasks,
  taskCreated,
  taskReplaced
} = tasksSlice.actions;
export default tasksSlice.reducer;
