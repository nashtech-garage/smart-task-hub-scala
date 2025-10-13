import { useCallback, useEffect, useState } from 'react';
import { useDispatch } from 'react-redux';
import { fetchBoardDetail } from '@/services/boardService';
import { notify } from '@/services/toastService';
import { connectToProjectWS, disconnectWS } from '@/services/websocketService';
import { reopenBoard as reopenBoardService } from '@/services/workspaceService';
import { store } from '@/store';
import { fetchColumns } from '@/store/thunks/columnsThunks';
import { fetchTasks } from '@/store/thunks/tasksThunks';
import { handleBoardWSMessage } from '@/websocket/boardHandler';
import type { Board } from '@/types';
import { fetchMembers } from '@/store/thunks/memberThunks';

interface UseBoardDataReturn {
    boardDetail: Board;
    isLoading: boolean;
    isBoardClosed: boolean;
    setIsBoardClosed: (closed: boolean) => void;
    refetch: () => Promise<void>;
    reopenBoard: () => Promise<void>;
    setIsLoading: (loading: boolean) => void;
}

/**
 * Custom hook for managing board data, WebSocket connection, and board status
 * 
 * Responsibilities:
 * - Fetch board detail, columns, and tasks
 * - Manage WebSocket connection for real-time updates
 * - Handle board open/close status
 * - Provide refetch functionality for data refresh
 * 
 * @param boardId - The ID of the board to manage
 * @returns Board data and management functions
 */
export const useBoardData = (boardId: number): UseBoardDataReturn => {
    const dispatch = useDispatch();

    // State
    const [boardDetail, setBoardDetail] = useState<Board>({ id: 0, name: '', status: undefined });
    const [isLoading, setIsLoading] = useState(false);
    const [isBoardClosed, setIsBoardClosed] = useState(false);

    /**
     * Fetch board detail and related data (columns, tasks)
     * Only fetches columns and tasks if board status is 'active'
     */
    const fetchBoardData = useCallback(async () => {
        if (!boardId) {
            notify.error('Invalid board ID');
            return;
        }

        setIsLoading(true);

        try {
            // Fetch board detail first
            const boardData = await fetchBoardDetail(boardId);
            const board = boardData.data;

            setBoardDetail(board);
            setIsBoardClosed(board?.status === 'completed');

            // Only fetch columns and tasks if board is active
            if (board?.status === 'active') {
                // Fetch in parallel for better performance
                await Promise.all([
                    dispatch(fetchColumns(boardId) as any),
                    dispatch(fetchTasks(boardId) as any),
                    dispatch(fetchMembers(boardId) as any),
                ]);
            }
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || 'Failed to fetch board data';
            notify.error(errorMessage);
            console.error('Error fetching board data:', error);
        } finally {
            setIsLoading(false);
        }
    }, [boardId, dispatch]);

    /**
     * Setup WebSocket connection for real-time updates
     * Automatically connects on mount and disconnects on unmount
     */
    useEffect(() => {
        if (!boardId) return;

        // Initial data fetch
        fetchBoardData();

        // Setup WebSocket connection
        connectToProjectWS(boardId, (message) => {
            handleBoardWSMessage(message, dispatch, store.getState);
        });

        // Cleanup: disconnect WebSocket on unmount
        return () => {
            disconnectWS();
        };
    }, [boardId, dispatch, fetchBoardData]);

    /**
     * Reopen a closed board
     * Automatically refetches board data after reopening
     */
    const reopenBoard = useCallback(async () => {
        if (!isBoardClosed) {
            notify.info('Board is already open');
            return;
        }

        setIsLoading(true);

        try {
            const result = await reopenBoardService(boardId);
            notify.success(result.message);

            // Refetch board data to get updated status and load columns/tasks
            await fetchBoardData();
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || 'Failed to reopen board';
            notify.error(errorMessage);
            console.error('Error reopening board:', error);
        } finally {
            setIsLoading(false);
        }
    }, [boardId, isBoardClosed, fetchBoardData]);

    return {
        boardDetail,
        isLoading,
        isBoardClosed,
        setIsBoardClosed,
        refetch: fetchBoardData,
        reopenBoard,
        setIsLoading
    };
};