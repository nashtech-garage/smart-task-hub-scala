import { Folder, X } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import LoadingContent from '../ui/LoadingContent';
import type { WorkSpace } from '@/types/workspace';
import { deleteBoard, getClosedBoards, reopenBoard } from '@/services/workspaceService';
import ClosedBoardItem from './ClosedBoardItem';
import { notify } from '@/services/toastService';

interface ClosedBoardsProp {
    hideClosedBoards: () => void
}

const ClosedBoards: React.FC<ClosedBoardsProp> = ({
    hideClosedBoards
}) => {

    const [closedBoards, setClosedBoards] = useState<WorkSpace[]>([]);
    const [loadingClosedModal, setLoadingClosedModal] = useState(false);
    const [activeConfirmation, setActiveConfirmation] = useState<{ id: number, type: 'delete' | 'reopen' } | null>(null);

    const fetchClosedBoards = async () => {
        setLoadingClosedModal(true);
        await getClosedBoards()
            .then(data => {
                data?.data && setClosedBoards(data.data)
                setLoadingClosedModal(false);
            })
            .catch(err => notify.error(err?.message))
    };

    const handleReopenBoard = async (boardId: number) => {
        await reopenBoard(boardId)
            .then(data => notify.success(data.message));
        fetchClosedBoards();
        setActiveConfirmation(null);
    };

    const handleDeleteBoard = async (boardId: number) => {
        await deleteBoard(boardId)
            .then(data => notify.success(data.message));
        fetchClosedBoards();
        setActiveConfirmation(null);
    };

    const handleShowConfirmation = (boardId: number, type: 'delete' | 'reopen') => {
        setActiveConfirmation({ id: boardId, type });
    };

    const handleHideConfirmation = () => {
        setActiveConfirmation(null);
    };

    useEffect(() => {
        fetchClosedBoards();
    }, []);

    return (
        <>
            {/* Backdrop */}
            <div
                className="fixed inset-0 bg-black/50 z-50"
                onClick={hideClosedBoards}
            />

            {/* Modal */}
            <div className="fixed top-0 left-[50%] -translate-x-[50%] w-[768px] flex items-center justify-center z-50 p-4">
                <div className="bg-[#2c3440] rounded-lg shadow-xl w-full max-w-2xl border border-gray-600">
                    {/* Modal Header */}
                    <div className="flex items-center justify-between p-4 border-b border-gray-600">
                        <div className="flex items-center space-x-2">
                            <Folder className="w-5 h-5 text-gray-300" />
                            <h2 className="text-white font-medium text-lg">Closed boards</h2>
                        </div>
                        <button
                            onClick={hideClosedBoards}
                            className="text-gray-400 hover:text-white cursor-pointer transition-colors p-1 rounded"
                        >
                            <X className="w-5 h-5" />
                        </button>
                    </div>

                    {/* Modal Body */}
                    <div className="p-4 m-h-[24px] overflow-y-auto">
                        {
                            loadingClosedModal ?
                                <LoadingContent /> :
                                closedBoards.length === 0 ? (
                                    <div className="text-center py-8">
                                        <p className="text-gray-400">No closed boards found</p>
                                    </div>
                                ) : (
                                    <div className="relative space-y-3">
                                        {closedBoards.map((board) => (
                                            <ClosedBoardItem
                                                key={board.id}
                                                workspace={board}
                                                handleDeleteBoard={handleDeleteBoard}
                                                handleReopenBoard={handleReopenBoard}
                                                activeConfirmation={activeConfirmation}
                                                onShowConfirmation={handleShowConfirmation}
                                                onHideConfirmation={handleHideConfirmation}
                                            />
                                        ))}
                                    </div>
                                )
                        }
                    </div>
                </div>
            </div>
        </>
    );
};

export default ClosedBoards;
