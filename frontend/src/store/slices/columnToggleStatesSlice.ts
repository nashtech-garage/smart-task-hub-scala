import { createSlice } from '@reduxjs/toolkit';
const columnToggleStatesSlice = createSlice({
  name: 'columnToggleStates',
  initialState: { isInputtingTitle: false, isAddingTask: false, isShowingOptionMenu: false, columnId: null },
  reducers: {
    showInputTitleForm: (state, action) => {
      state.isInputtingTitle = true;
      state.isAddingTask= false;
      state.isShowingOptionMenu = false;
      state.columnId = action.payload;
    },

    showAddTaskForm: (state, action) => {
      state.isAddingTask = true;
      state.isInputtingTitle = false;
      state.isShowingOptionMenu = false;
      state.columnId = action.payload;
    },

    showOptionMenu: (state, action) => {
      state.isShowingOptionMenu = true;
      state.isInputtingTitle = false;
      state.isAddingTask = false;
      state.columnId = action.payload;
    },

    resetcolumnToggleStates: (state) => {
      state.isInputtingTitle = false;
      state.isAddingTask = false;
      state.isShowingOptionMenu = false;
      state.columnId = null;
    }
  }
});
export const { showInputTitleForm, showAddTaskForm, showOptionMenu, resetcolumnToggleStates } = columnToggleStatesSlice.actions;
export default columnToggleStatesSlice.reducer;
