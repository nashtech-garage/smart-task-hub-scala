import type { AppThunk } from "..";
import taskService from "@/services/taskService";
import { notify } from "@/services/toastService";
import { setArchivedTasks } from "../slices/archiveTasksSlice";

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
