import { fetchBoardMembers } from "@/services/boardService";
import type { AppThunk } from "..";
import { setMembers } from "../slices/membersSlice";

export const fetchMembers = (projectId: number): AppThunk => async (dispatch) => {
    try {
        const res = await fetchBoardMembers(projectId);

        dispatch(setMembers(res.data));
    } catch (error) {
        console.error("fetchMembers error:", error);
    }
};