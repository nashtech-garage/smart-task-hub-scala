import { notify } from "@/services/toastService";
import { createWorkspace } from "@/services/workspaceService";
import { useState, type SetStateAction } from "react";

interface CreateWorkspaceModalProps {
    isOpen: boolean;
    onClose: () => void;
}

const CreateWorkspaceModal: React.FC<CreateWorkspaceModalProps> = ({
    isOpen,
    onClose,
}) => {
    const [workspaceName, setWorkspaceName] = useState('');
    const [workspaceDescription, setWorkspaceDescription] = useState<string>('');
    const [workspaceNameError, setWorkspaceNameError] = useState('');
    const [workspaceDescriptionError, setWorkspaceDescriptionError] = useState('');

    const handleSubmit = async () => {
        if (workspaceName.trim()) {
            await createWorkspace(
                workspaceName,
                workspaceDescription
            ).then(data => {
                notify.success(data.message);
                // Reset form fields
                setWorkspaceName('');
                setWorkspaceDescription('');
                // Reset errors
                setWorkspaceNameError('');
                setWorkspaceDescriptionError('');
                onClose();
            })
            .catch(err => {
                const errorFields = err?.response?.data?.errors || [];
                notify.error(errorFields);
                // Set error messages based on the response
                errorFields.forEach((element: { field: string; message: SetStateAction<string>; }) => {
                    switch (element.field) {
                        case 'name':
                            setWorkspaceNameError(element.message);
                            break;
                        case 'description':
                            setWorkspaceDescriptionError(element.message);
                            break;
                        }
                    notify.error(element.message.toString());
                });
            });
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-white/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden">
                <div className="flex">
                    {/* Left side - Form */}
                    <div className="flex-1 p-8 bg-gray-800 text-white">
                        <button
                            onClick={onClose}
                            className="absolute top-4 right-4 text-gray-400 hover:text-white"
                        >
                            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>

                        <div className="mb-8">
                            <h1 className="text-3xl font-semibold mb-2">Let's build a Workspace</h1>
                            <p className="text-gray-300">
                                Boost your productivity by making it easier for everyone to access boards in one location.
                            </p>
                        </div>

                        <div className="space-y-6">
                            {/* Workspace Name */}
                            <div>
                                <label className="block text-sm font-medium mb-2">
                                    Workspace name
                                </label>
                                <div className="mb-2">
                                    {workspaceNameError && (
                                        <p className="text-red-500 text-sm mb-2">
                                            {workspaceNameError}
                                        </p>
                                    )}
                                </div>
                                <input
                                    type="text"
                                    value={workspaceName}
                                    onChange={(e) => setWorkspaceName(e.target.value)}
                                    placeholder="Taco's Co."
                                    className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    required
                                />
                                <p className="text-xs text-gray-400 mt-1">
                                    This is the name of your company, team or organization.
                                </p>
                            </div>

                            {/* Workspace Description */}
                            <div>
                                <label className="block text-sm font-medium mb-2">
                                    Workspace description 
                                </label>
                                <div className="mb-2">
                                    {workspaceDescriptionError && (
                                        <p className="text-red-500 text-sm mb-2">
                                            {workspaceDescriptionError}
                                        </p>
                                    )}
                                </div>
                                <textarea
                                    value={workspaceDescription || ''}
                                    onChange={(e) => setWorkspaceDescription(e.target.value || '')}
                                    placeholder="Our team organizes everything here."
                                    rows={4}
                                    className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                />
                                <p className="text-xs text-gray-400 mt-1">
                                    Get your members on board with a few words about your Workspace.
                                </p>
                            </div>

                            {/* Submit Button */}
                            <button
                                onClick={handleSubmit}
                                disabled={!workspaceName.trim()}
                                className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white py-2 px-4 rounded-md font-medium transition-colors"
                            >
                                Continue
                            </button>
                        </div>
                    </div>

                    {/* Right side - Illustration */}
                    <div className="flex-1 bg-gradient-to-br from-blue-100 to-green-100 flex items-center justify-center p-8">
                        <div className="relative">
                            {/* Decorative elements */}
                            <div className="absolute -top-8 -left-8 w-6 h-6 bg-blue-300 rounded-full opacity-60"></div>
                            <div className="absolute -top-4 -right-12 w-4 h-4 bg-green-300 rounded-full opacity-60"></div>
                            <div className="absolute -bottom-6 -left-6 w-3 h-3 bg-yellow-300 rounded-full opacity-60"></div>
                            <div className="absolute -bottom-4 -right-8 w-5 h-5 bg-pink-300 rounded-full opacity-60"></div>

                            {/* Main illustration - Trello board mockup */}
                            <div className="bg-teal-400 rounded-lg p-6 shadow-lg transform rotate-3">
                                <div className="bg-white rounded p-2 mb-2">
                                    <div className="flex items-center justify-between mb-3">
                                        <div className="w-16 h-2 bg-gray-300 rounded"></div>
                                        <div className="flex space-x-1">
                                            <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
                                            <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
                                            <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
                                        </div>
                                    </div>

                                    <div className="grid grid-cols-3 gap-3">
                                        {/* Column 1 */}
                                        <div className="space-y-2">
                                            <div className="bg-gray-100 rounded p-2">
                                                <div className="w-full h-2 bg-gray-300 rounded mb-1"></div>
                                                <div className="w-3/4 h-2 bg-gray-300 rounded"></div>
                                            </div>
                                            <div className="bg-gray-100 rounded p-2">
                                                <div className="w-full h-2 bg-gray-300 rounded mb-1"></div>
                                                <div className="w-1/2 h-2 bg-gray-300 rounded"></div>
                                            </div>
                                            <div className="w-4 h-4 bg-green-400 rounded-full mx-auto"></div>
                                        </div>

                                        {/* Column 2 */}
                                        <div className="space-y-2">
                                            <div className="bg-gray-100 rounded p-2">
                                                <div className="w-full h-2 bg-gray-300 rounded mb-1"></div>
                                                <div className="w-2/3 h-2 bg-gray-300 rounded"></div>
                                            </div>
                                            <div className="w-4 h-4 bg-orange-400 rounded-full mx-auto"></div>
                                        </div>

                                        {/* Column 3 */}
                                        <div className="space-y-2">
                                            <div className="bg-gray-100 rounded p-2">
                                                <div className="w-full h-2 bg-gray-300 rounded mb-1"></div>
                                                <div className="w-5/6 h-2 bg-gray-300 rounded mb-1"></div>
                                                <div className="w-1/3 h-2 bg-gray-300 rounded"></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Backdrop */}
            <div
                className="absolute inset-0 -z-10"
                onClick={onClose}
            ></div>
        </div>
    );
};

export default CreateWorkspaceModal;