import { createSlice } from '@reduxjs/toolkit';
const taskModalSlice = createSlice({
  name: 'taskModal',
  initialState: { isTaskModalShow: false, taskId: null },
  reducers: {
    showTaskModal: (state, action) => {
      state.isTaskModalShow = true;
      state.taskId = action.payload;
    },
    hideTaskModal: (state) => {
      state.isTaskModalShow = false;
      state.taskId = null;
    }
  }
});
export const { showTaskModal, hideTaskModal } = taskModalSlice.actions;
export default taskModalSlice.reducer;
