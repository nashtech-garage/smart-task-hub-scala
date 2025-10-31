import { createBoard } from '@/services/workspaceService';
import { Plus, TriangleAlert } from 'lucide-react';
import { useState } from 'react';
import { notify } from '@/services/toastService';

const backgroundOptions = [
    { type: 'image', url: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=300&h=200&fit=crop' },
    { type: 'image', url: 'https://images.unsplash.com/photo-1464822759844-d150baec93c5?w=300&h=200&fit=crop' },
    { type: 'image', url: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=300&h=200&fit=crop' },
    { type: 'image', url: 'https://images.unsplash.com/photo-1472214103451-9374bd1c798e?w=300&h=200&fit=crop' },
    { type: 'color', color: 'bg-gray-200' },
    { type: 'color', color: 'bg-blue-400' },
    { type: 'color', color: 'bg-blue-600' },
    { type: 'color', color: 'bg-purple-400' },
    { type: 'color', color: 'bg-pink-400' }
];

type CreateBoardProp = {
    id: number | null;
    workspaceName: string;
    onBoardCreated?: () => void;
};

const CreateBoard: React.FC<CreateBoardProp> = ({ id, workspaceName, onBoardCreated }) => {

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [boardTitle, setBoardTitle] = useState('');
    const [selectedBackground, setSelectedBackground] = useState(0);
    const [errorMessage, setErrorMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleCreateBoard = async () => {
        if (!id) return;
        if (boardTitle.trim()) {
            setIsLoading(true);
            setErrorMessage(''); // Clear any previous errors

            await createBoard(boardTitle, id)
                .then(res => {
                    notify.success(res.message);
                    setShowCreateModal(false);
                    onBoardCreated?.()
                    setBoardTitle('');
                    setSelectedBackground(0);
                    setErrorMessage(''); // Clear errors on success
                })
                .catch(err => {
                    // Handle different error response structures
                    const apiErrorMessage = err.response?.data?.errors?.[0]?.message
                        || err.response?.data?.message
                        || err.message
                        || 'An error occurred while creating the board';
                    setErrorMessage(apiErrorMessage);
                    notify.error(apiErrorMessage);
                })
                .finally(() => {
                    setIsLoading(false);
                });
        }
    };

    const handleCloseModal = () => {
        setShowCreateModal(false);
        setBoardTitle('');
        setSelectedBackground(0);
        setErrorMessage(''); // Clear errors when closing
    };

    const handleTitleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setBoardTitle(e.target.value);
        // Clear error message when user starts typing
        if (errorMessage) {
            setErrorMessage('');
        }
    };

    // Determine what error to show
    const getErrorToDisplay = () => {
        if (errorMessage) {
            return errorMessage; // Show API error message
        }
        if (!boardTitle.trim()) {
            return 'Board title is required'; // Show validation error
        }
        return null;
    };

    const errorToDisplay = getErrorToDisplay();

    return (
        <>
            <div
                onClick={() => setShowCreateModal(true)}
                className="
                    h-24 bg-[#2A2D31] hover:bg-[#3A3D41] rounded-lg cursor-pointer transition-colors 
                    flex items-center justify-center border-2 border-dashed border-gray-600"
            >
                <span className="text-gray-400 font-medium mr-0.5"><Plus /></span>
                <span className="text-gray-400 font-medium">Create new board</span>
            </div>
            {/* Create Board Modal */}
            {showCreateModal && (
                <div className="fixed inset-0 flex items-center justify-center z-50">
                    <div className="bg-[#2A2D31] rounded-lg p-0 w-[300px] max-w-md mx-4 overflow-hidden">
                        {/* Modal Header */}
                        <div className="flex items-center justify-between p-4 border-b border-gray-600">
                            <h3 className="text-lg font-medium text-white">Create board</h3>
                            <button
                                onClick={handleCloseModal}
                                className="text-gray-400 hover:text-white text-2xl leading-none"
                            >
                                ×
                            </button>
                        </div>

                        <div className="p-4">
                            {/* Background Preview */}
                            <div className="mb-4">
                                <div
                                    className={`w-full h-20 rounded-lg mb-3 ${backgroundOptions[selectedBackground]?.type === 'image' ? 'bg-cover bg-center' : backgroundOptions[selectedBackground]?.color || 'bg-gray-600'
                                        }`}
                                    style={backgroundOptions[selectedBackground]?.type === 'image' ? {
                                        backgroundImage: `url(${backgroundOptions[selectedBackground]?.url})`
                                    } : {}}
                                >
                                    <div className="w-full h-full bg-opacity-20 rounded-lg flex items-center justify-center"></div>
                                </div>

                                <label className="block text-sm text-gray-300 mb-2">Background</label>
                                <div className="grid grid-cols-4 gap-2">
                                    {backgroundOptions.map((option, index) => (
                                        <div
                                            key={index}
                                            onClick={() => setSelectedBackground(index)}
                                            className={`w-12 h-8 rounded cursor-pointer border-2 ${selectedBackground === index ? 'border-blue-400' : 'border-transparent'
                                                } ${option.type === 'image' ? 'bg-cover bg-center' : option.color}`}
                                            style={option.type === 'image' ? {
                                                backgroundImage: `url(${option.url})`
                                            } : {}}
                                        />
                                    ))}
                                    <div className="w-12 h-8 rounded cursor-pointer border-2 border-transparent bg-gray-600 flex items-center justify-center text-gray-400 text-xs">
                                        ⋯
                                    </div>
                                </div>
                            </div>

                            {/* Board Title Input */}
                            <div className="mb-4">
                                <label className="block text-sm text-gray-300 mb-2">
                                    Board title <span className="text-red-400">*</span>
                                </label>
                                <input
                                    type="text"
                                    value={boardTitle}
                                    onChange={handleTitleChange}
                                    className={`w-full px-3 py-2 bg-[#1E2125] border rounded text-white text-sm focus:outline-none ${errorToDisplay ? 'border-red-400 focus:border-red-400' : 'border-gray-600 focus:border-blue-400'
                                        }`}
                                    placeholder="Enter board title"
                                    disabled={isLoading}
                                />
                                {errorToDisplay && (
                                    <div className="flex items-center gap-2 mt-2 text-red-400">
                                        <TriangleAlert size={16} />
                                        <span className="text-sm">
                                            {errorToDisplay}
                                        </span>
                                    </div>
                                )}
                            </div>

                            {/* Workspace Dropdown */}
                            <div className="mb-4">
                                <label className="block text-sm text-gray-300 mb-2">Workspace</label>
                                <span className="block opacity-60 cursor-not-allowed px-3 py-2 bg-[#1E2125] border border-gray-600 rounded text-white text-sm">
                                    {workspaceName}
                                </span>
                            </div>

                            {/* Action Buttons */}
                            <div className="flex flex-col space-y-3">
                                <button
                                    onClick={handleCreateBoard}
                                    disabled={!boardTitle.trim() || isLoading}
                                    className={`w-full py-2 text-sm rounded font-medium ${boardTitle.trim() && !isLoading
                                            ? 'bg-blue-600 hover:bg-blue-700 text-white'
                                            : 'bg-gray-600 text-gray-400 cursor-not-allowed'
                                        }`}
                                >
                                    {isLoading ? 'Creating...' : 'Create'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

        </>
    );
}

export default CreateBoard;