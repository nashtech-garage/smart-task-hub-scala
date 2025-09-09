import { notify } from "@/services/toastService";
import { deleteWorkspace, getWorkspaceById } from "@/services/workspaceService";
import type { WorkSpace } from "@/types/workspace";
import { EarthIcon, LockKeyhole } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

const Setting = () => {

    const { id } = useParams();
    const navigate = useNavigate();
    const [workspaceData, setWorkSpaceData] = useState<WorkSpace>({
        id: Number(id),
        name: '',
        desc: '',
    });
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteInputValue, setDeleteInputValue] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedVisibility, setSelectedVisibility] = useState("private");

    const fetchWorkspaceDetail = async () => {
        if (!id) return;
        await getWorkspaceById(Number(id))
            .then(data => {
                if (!data?.data) throw new Error("Workspace not found");
                setWorkSpaceData(data.data);
            })
            .catch(_ => navigate('/not-found'));
    };

    const handleSave = () => {
        console.log("Selected visibility:", selectedVisibility);
        setShowModal(false);
    };

    const handleDeleteClick = () => {
        setShowDeleteModal(true);
    };

    const handleDeleteConfirm = async () => {
        if (!id) return;
        if (deleteInputValue === workspaceData.name) {
            setShowDeleteModal(false);
            setDeleteInputValue('');
            await deleteWorkspace(Number(id))
                .then(data => {
                    notify.success(data.message);
                    navigate('/')
                })
                .catch(err => notify.error(err?.message));
        }
    };

    const handleDeleteCancel = () => {
        setShowDeleteModal(false);
        setDeleteInputValue('');
    };

    useEffect(() => {
        fetchWorkspaceDetail();
    }, [id]);

    return (
        <div className="
            h-full flex flex-col justify-items-start
            bg-[#1E2125] text-white p-8"
        >
            {/* Header */}
            <div className="mb-8">
                <h1 className="text-2xl font-semibold mb-6">Workspace settings</h1>
                {/* Workspace Info */}
                <div className="flex items-center mb-8">
                    <div className="w-12 h-12 bg-orange-500 rounded flex items-center justify-center text-black font-bold text-xl mr-4">
                        {workspaceData.name.charAt(0)}
                    </div>
                    <div>
                        <h2 className="text-lg font-medium">{workspaceData.name}</h2>
                        <span className="text-sm text-gray-400">{workspaceData.desc}</span>
                    </div>
                </div>
            </div>

            {/* Workspace visibility */}
            <div className="mb-8 pb-6 border-b border-gray-700">
                <h3 className="text-lg font-medium mb-4">Workspace visibility</h3>
                <div className="flex items-center justify-between p-4 rounded-lg">
                    <div className="flex items-center gap-2">
                        {
                            selectedVisibility === 'private' ?
                                <>
                                    <LockKeyhole />
                                    <span className="text-sm">Private – This Workspace is private. It's not indexed or visible to those outside the Workspace.</span>
                                </> :
                                <>
                                    <EarthIcon />
                                    <span className="text-sm"> Public – This Workspace is public. It's visible to anyone with the link and will show up in search engines like Google. Only those invited to the Workspace can add and edit Workspace boards.</span>
                                </>
                        }
                    </div>
                    <div className="relative">
                        <button
                            onClick={() => setShowModal(true)}
                            className="text-blue-400 cursor-pointer hover:text-blue-300 text-sm font-medium">
                            Change
                        </button>
                        {/* Modal */}
                        {showModal && (
                            <div className="absolute top-full right-0 flex items-center justify-center z-50">
                                <div className="bg-[#2A2D31] rounded-lg p-6 w-96 max-w-md">
                                    {/* Modal Header */}
                                    <div className="flex items-center justify-between mb-6">
                                        <h3 className="text-lg font-medium">Select Workspace visibility</h3>
                                        <button
                                            onClick={() => setShowModal(false)}
                                            className="text-gray-400 hover:text-white cursor-pointer"
                                        >
                                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                            </svg>
                                        </button>
                                    </div>

                                    {/* Visibility Options */}
                                    <div className="space-y-4 mb-6">
                                        {/* Private Option */}
                                        <label className="flex items-start cursor-pointer">
                                            <input
                                                type="radio"
                                                name="visibility"
                                                value="private"
                                                checked={selectedVisibility === "private"}
                                                onChange={(e) => setSelectedVisibility(e.target.value)}
                                                className="mt-1 mr-3 text-blue-500"
                                            />
                                            <div className="flex-1">
                                                <div className="flex items-center mb-1">
                                                    <svg className="w-4 h-4 mr-2 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                                    </svg>
                                                    <span className="font-medium">Private</span>
                                                </div>
                                                <p className="text-sm text-gray-400">
                                                    This Workspace is private. It's not indexed or visible to those outside the Workspace.
                                                </p>
                                            </div>
                                        </label>

                                        {/* Public Option */}
                                        <label className="flex items-start cursor-pointer">
                                            <input
                                                type="radio"
                                                name="visibility"
                                                value="public"
                                                checked={selectedVisibility === "public"}
                                                onChange={(e) => setSelectedVisibility(e.target.value)}
                                                className="mt-1 mr-3 text-blue-500"
                                            />
                                            <div className="flex-1">
                                                <div className="flex items-center mb-1">
                                                    <svg className="w-4 h-4 mr-2 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                                    </svg>
                                                    <span className="font-medium">Public</span>
                                                </div>
                                                <p className="text-sm text-gray-400">
                                                    This Workspace is public. It's visible to anyone with the link and will show up in search engines like Google. Only those invited to the Workspace can add and edit Workspace boards.
                                                </p>
                                            </div>
                                        </label>
                                    </div>

                                    {/* Modal Actions */}
                                    <div className="flex justify-end space-x-3">
                                        <button
                                            onClick={() => setShowModal(false)}
                                            className="px-4 py-2 text-gray-400 hover:text-white transition-colors"
                                        >
                                            Cancel
                                        </button>
                                        <button
                                            onClick={handleSave}
                                            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
                                        >
                                            Save
                                        </button>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Delete workspace */}
            <div className="relative pt-6 mt-auto">
                <button
                    onClick={handleDeleteClick}
                    className="text-red-400 hover:text-red-300 text-sm font-medium underline">
                    Delete this Workspace?
                </button>
                {/* Delete Confirmation Modal */}
                {showDeleteModal && (
                    <div className="absolute bottom-full left-0 flex items-center justify-center z-50">
                        <div className="bg-[#2A2D31] rounded-lg p-6 w-[480px] max-w-md">
                            <div className="flex items-center justify-between mb-4">
                                <h3 className="text-lg font-medium text-white">Delete Workspace?</h3>
                                <button
                                    onClick={handleDeleteCancel}
                                    className="text-gray-400 hover:text-white text-2xl leading-none"
                                >
                                    ×
                                </button>
                            </div>

                            <div className="mb-6">
                                <p className="text-sm text-gray-300 mb-4">
                                    Enter the Workspace name <strong>"{workspaceData.name}"</strong> to delete:
                                </p>

                                <div className="mb-4">
                                    <p className="text-sm text-gray-400 mb-3">Things to know:</p>
                                    <ul className="text-sm text-gray-400 space-y-2">
                                        <li>• This is permanent and can't be undone.</li>
                                        <li>• <span className="underline">All boards in this Workspace will be closed</span>.</li>
                                        <li>• Board admins can reopen boards.</li>
                                        <li>• Board members will not be able to interact with closed boards.</li>
                                    </ul>
                                </div>

                                <p className="text-sm text-gray-300 mb-2">
                                    Enter the Workspace name to delete:
                                </p>
                                <input
                                    type="text"
                                    value={deleteInputValue}
                                    onChange={(e) => setDeleteInputValue(e.target.value)}
                                    className="w-full px-3 py-2 bg-[#1E2125] border border-gray-600 rounded text-white text-sm focus:outline-none focus:border-blue-400"
                                    placeholder=""
                                />
                            </div>

                            <div className="flex justify-end space-x-3">
                                <button
                                    onClick={handleDeleteConfirm}
                                    disabled={deleteInputValue !== workspaceData.name}
                                    className={`px-4 py-2 text-sm rounded font-medium ${deleteInputValue === workspaceData.name
                                        ? 'bg-red-600 hover:bg-red-700 text-white'
                                        : 'bg-gray-600 text-gray-400 cursor-not-allowed'
                                        }`}
                                >
                                    Delete Workspace
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Setting;

