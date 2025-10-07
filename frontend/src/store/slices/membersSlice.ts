import type { Member } from "@/types";
import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

const initialState: Member[] = [];

const membersSlice = createSlice({
  name: 'members',
  initialState,
  reducers: {
    setMembers: (state, action: PayloadAction<Member[]>) => {
      return action.payload;
    },
  },
});

export const { setMembers } = membersSlice.actions;
export default membersSlice.reducer;