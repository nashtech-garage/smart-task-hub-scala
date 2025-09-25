import { configureStore } from '@reduxjs/toolkit';
import { persistStore, persistReducer } from 'redux-persist';
import storage from 'redux-persist/lib/storage';
import { combineReducers } from '@reduxjs/toolkit';
import authSlice from './slices/authSlice';
import columnsReducer from "./slices/columnsSlice";
import tasksReducer from "./slices/tasksSlice";
import archivedColumnsReducer from "./slices/archiveColumnsSlice";
import archivedTasksReducer from "./slices/archiveTasksSlice";
import { useDispatch, useSelector, type TypedUseSelectorHook } from 'react-redux';

// Persist configuration
const persistConfig = {
    key: 'root',
    storage,
    whitelist: ['auth'], // Persist auth data including role
};

const rootReducer = combineReducers({
    auth: authSlice,
    columns: columnsReducer,
    tasks: tasksReducer,
    archivedColumns: archivedColumnsReducer,
    archivedTasks: archivedTasksReducer,
});

const persistedReducer = persistReducer(persistConfig, rootReducer);

export const store = configureStore({
    reducer: persistedReducer,
    middleware: getDefaultMiddleware =>
        getDefaultMiddleware({
            serializableCheck: {
                ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
            },
        }),
    devTools: process.env.NODE_ENV !== 'production',
});

export const persistor = persistStore(store);

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export type AppThunk<ReturnType = void> = (
  dispatch: AppDispatch,
  getState: () => RootState
) => ReturnType;

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;