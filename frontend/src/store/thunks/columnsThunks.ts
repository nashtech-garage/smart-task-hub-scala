import { archiveColumn, createNewColumn, fetchArchivedColumns, fetchBoardColumns, restoreColumn } from "@/services/boardService";
import type { AppThunk } from "..";
import { columnCreated, columnRemoved, columnRestored, setColumns } from "../slices/columnsSlice";
import { notify } from "@/services/toastService";
import type { Column } from "@/types";
import { archivedColumnRestored, columnArchived, setArchivedColumns } from "../slices/archiveColumnsSlice";

export const fetchColumns = (projectId: number): AppThunk => async (dispatch) => {
    try {
        const res = await fetchBoardColumns(Number(projectId));

        dispatch(setColumns(res.data));
    } catch (error) {
        console.error("fetchColumns error:", error);
    }
};

export const addColumnThunk =
    (boardId: number, name: string, position: number): AppThunk =>
        async (dispatch) => {
            const tempId = - position;
            dispatch(columnCreated({ id: tempId, name, position, taskIds: [] }));
            try {
                await createNewColumn(boardId, name, position);
            } catch (err) {
                dispatch(columnRemoved(tempId));
                notify.error("Create column failed");
            }
        };

export const archiveColumnThunk =
    (column: Column): AppThunk =>
        async (dispatch) => {
            try {
                dispatch(columnRemoved(column.id));
                dispatch(columnArchived(column));
                await archiveColumn(column.id);
            } catch (err) {
                dispatch(columnCreated(column));
                console.error("Archive column failed", err);
            }
        };

export const restoreColumnThunk =
    (column: Column): AppThunk =>
        async (dispatch) => {
            try {
                dispatch(archivedColumnRestored(column.id));
                dispatch(columnRestored(column));
                await restoreColumn(column.id);
            } catch (err) {
                dispatch(columnRemoved(column.id));
                console.error("Restore column failed", err);
            }
        };

export const fetchArchivedColumnsThunk =
  (boardId: number): AppThunk =>
  async (dispatch) => {
    try {
      const res = await fetchArchivedColumns(boardId);
      if (res.data) {
        dispatch(setArchivedColumns(res.data));
      }
    } catch (error: any) {
      notify.error(error.response?.data?.message);
    }
  };