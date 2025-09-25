import { fetchActiveBoardTasks } from "@/services/boardService";
import type { AppThunk } from "..";
import { normalizeTasks } from "@/utils/normalize";
import { setTasks } from "../slices/tasksSlice";
import taskService from "@/services/taskService";
import { notify } from "@/services/toastService";
import { setArchivedTasks } from "../slices/archiveTasksSlice";

export const fetchTasks = (projectId: number): AppThunk => async (dispatch) => {
    try {
        const res = await fetchActiveBoardTasks(Number(projectId));

        const normalized = normalizeTasks(res.data);
        dispatch(setTasks(normalized));
    } catch (error) {
        console.error("fetch tasks error:", error);
    }
};

export const fetchArchivedTasksThunk =
  (boardId: number): AppThunk =>
  async (dispatch) => {
    try {
      const res = await taskService.getArchivedTasks(boardId);
      if (res.data) {
        dispatch(setArchivedTasks(res.data));
      }
    } catch (error: any) {
      notify.error(error.response?.data?.message);
    }
  };
