import type { RootState } from "..";

export const selectTaskModalState = (state: RootState) => state.taskModal;

export const selectColumnToggleStates = (state: RootState) => state.columnToggleStates;