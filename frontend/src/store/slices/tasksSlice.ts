import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { Task, TaskDetail, TasksState } from "@/types";

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
    taskUpdated: (state, action: PayloadAction<{ taskId: number; columnId: string; taskPosition: number; detail: TaskDetail }>) => {
      const { taskId, detail, taskPosition } = action.payload;

      const task = state.byId[taskId];
      if (task) {
        task.detail = detail;
        task.name = detail.name;
        task.position = taskPosition;
      }
    },
    taskRemoved: (state, action: PayloadAction<number>) => {
      const id = action.payload;
      delete state.byId[id];
      state.allIds = state.allIds.filter((cid) => cid !== id);
    },
    taskRestored: (state, action: PayloadAction<Task>) => {
      const task = action.payload;
      state.byId[task.id] = task;

      const index = state.allIds.findIndex(id => state.byId[id].position > task.position);
      if (index === -1) {
        console.log("Pushing task", task.id);
        state.allIds.push(task.id);
      } else {
        state.allIds.splice(index, 0, task.id);
        console.log("Inserting task", task.id, "at", index);
      }
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
  taskReplaced,
  taskUpdated,
  taskRemoved,
  taskRestored
} = tasksSlice.actions;
export default tasksSlice.reducer;
