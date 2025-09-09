import type { WorkSpace } from "@/types/workspace";
import { RotateCcw, Trash2, X } from "lucide-react";
import { useState } from "react";
import LoadingContent from "../ui/LoadingContent";
const backgroundImage = 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=300&h=200&fit=crop'

interface ClosedBoardItemProp {
    workspace: WorkSpace;
    handleReopenBoard: (boardId: number) => Promise<void>;
    handleDeleteBoard: (boardId: number) => Promise<void>;
    activeConfirmation: { id: number, type: 'delete' | 'reopen' } | null;
    onShowConfirmation: (boardId: number, type: 'delete' | 'reopen') => void;
    onHideConfirmation: () => void;
}

const ClosedBoardItem: React.FC<ClosedBoardItemProp> = ({
    workspace,
    handleDeleteBoard,
    handleReopenBoard,
    activeConfirmation,
    onShowConfirmation,
    onHideConfirmation
}) => {

    const { id, name, desc } = workspace;
    const [loadingDeleteConfirmModal, setLoadingDeleteConfirmModal] = useState(false);
    const [loadingReopenConfirmModal, setLoadingReopenConfirmModal] = useState(false);

    const isShowingDeleteConfirm = activeConfirmation?.id === id && activeConfirmation?.type === 'delete';
    const isShowingReopenConfirm = activeConfirmation?.id === id && activeConfirmation?.type === 'reopen';

    const deleteBoardAction = async () => {
        if (!id) return;
        setLoadingDeleteConfirmModal(true);
        await handleDeleteBoard(id)
        setLoadingDeleteConfirmModal(false);
    }

    const reopenBoardAction = async () => {
        if (!id) return;
        setLoadingReopenConfirmModal(true);
        await handleReopenBoard(id);
        setLoadingReopenConfirmModal(false);
    }

    return (
        <>
            <div className="flex items-center justify-between p-3 bg-[#1e2328] rounded-lg hover:bg-[#252a30] transition-colors">
                <div className="flex items-center space-x-3">
                    {/* Board Thumbnail */}
                    <div className="w-12 h-8 rounded bg-gray-600 overflow-hidden flex-shrink-0">
                        <img
                            src={backgroundImage}
                            alt={name}
                            className="w-full h-full object-cover"
                        />
                    </div>

                    {/* Board Info */}
                    <div>
                        <h3 className="text-blue-400 font-medium text-sm hover:underline cursor-pointer">
                            {name}
                        </h3>
                        <p className="text-gray-400 text-xs">
                            {desc}
                        </p>
                    </div>
                </div>

                {/* Action Buttons */}
                <div className="flex items-center space-x-2">
                    <div
                        onClick={() => onShowConfirmation(id, 'reopen')}
                        className="flex items-center cursor-pointer gap-2 px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded transition-colors"
                    >
                        <RotateCcw className="w-3 h-3" />
                        <span>Reopen</span>
                    </div>

                    <div
                        onClick={() => onShowConfirmation(id, 'delete')}
                        className="flex items-center gap-2 cursor-pointer px-3 py-1.5 bg-red-600 hover:bg-red-700 text-white text-sm rounded transition-colors"
                    >
                        <Trash2 className="w-3 h-3 shrink-0" />
                        <span>Delete</span>
                    </div>
                </div>
            </div>

            {/* Reopen Confirmation Modal - Fixed positioning with higher z-index */}
            {isShowingReopenConfirm && (
                <>
                    {/* Backdrop */}
                    <div
                        className="fixed inset-0 z-[60]"
                        onClick={onHideConfirmation}
                    />

                    {/* Modal */}
                    <div className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-[70] p-4">
                        <div className="bg-[#2c3440] rounded-lg shadow-xl w-[300px] border border-gray-600">
                            {/* Modal Header */}
                            <div className="flex items-center justify-between p-4 border-b border-gray-600">
                                <h2 className="text-white font-medium">Reopen board "{name}"?</h2>
                                <button
                                    onClick={onHideConfirmation}
                                    className="text-gray-400 hover:text-white transition-colors p-1 rounded"
                                >
                                    <X className="w-4 h-4" />
                                </button>
                            </div>

                            {/* Modal Body */}
                            <div className="p-4">
                                <p className="text-gray-300 text-sm mb-4 leading-relaxed">
                                    This board will be reopened and moved back to your active boards. You can close it again at any time.
                                </p>

                                <button
                                    onClick={reopenBoardAction}
                                    disabled={loadingReopenConfirmModal}
                                    className="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-white py-2.5 rounded font-medium transition-colors"
                                >
                                    {loadingReopenConfirmModal ? <LoadingContent /> : 'Reopen'}
                                </button>
                            </div>
                        </div>
                    </div>
                </>
            )}

            {/* Delete Confirmation Modal - Fixed positioning with higher z-index */}
            {isShowingDeleteConfirm && (
                <>
                    {/* Backdrop */}
                    <div
                        className="fixed inset-0 bg-black/30 z-[60]"
                        onClick={onHideConfirmation}
                    />

                    {/* Modal */}
                    <div className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-[70] p-4">
                        <div className="bg-[#2c3440] rounded-lg shadow-xl w-[300px] border border-gray-600">
                            {/* Modal Header */}
                            <div className="flex items-center justify-between p-4 border-b border-gray-600">
                                <h2 className="text-white font-medium">Delete board "{name}"?</h2>
                                <button
                                    onClick={onHideConfirmation}
                                    className="text-gray-400 hover:text-white transition-colors p-1 rounded"
                                >
                                    <X className="w-4 h-4" />
                                </button>
                            </div>

                            {/* Modal Body */}
                            <div className="p-4">
                                <p className="text-gray-300 text-sm mb-4 leading-relaxed">
                                    All lists, cards and actions will be deleted, and you won't be able to re-open the board. There is no undo.
                                </p>

                                <button
                                    onClick={deleteBoardAction}
                                    disabled={loadingDeleteConfirmModal}
                                    className="w-full bg-red-500 hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed text-white py-2.5 rounded font-medium transition-colors"
                                >
                                    {loadingDeleteConfirmModal ? <LoadingContent /> : 'Delete'}
                                </button>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </>
    )
};

export default ClosedBoardItem;