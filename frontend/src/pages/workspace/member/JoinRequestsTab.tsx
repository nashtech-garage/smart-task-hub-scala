import type { JoinRequest } from "@/types/collaboration";
import { UserPlus, UserX } from "lucide-react";
import { useState } from "react";

const JoinRequestsTab: React.FC<{ requests: JoinRequest[] }> = ({ requests }) => {
    const [selectedRequests, setSelectedRequests] = useState<Set<string>>(new Set());

    const toggleRequest = (id: string) => {
        const newSelected = new Set(selectedRequests);
        if (newSelected.has(id)) {
            newSelected.delete(id);
        } else {
            newSelected.add(id);
        }
        setSelectedRequests(newSelected);
    };

    const toggleAll = () => {
        if (selectedRequests.size === requests.length) {
            setSelectedRequests(new Set());
        } else {
            setSelectedRequests(new Set(requests.map(r => r.id)));
        }
    };

    const handleAddSelected = () => {
        console.log('Adding to workspace:', Array.from(selectedRequests));
        setSelectedRequests(new Set());
    };

    const handleDeleteSelected = () => {
        console.log('Deleting requests:', Array.from(selectedRequests));
        setSelectedRequests(new Set());
    };

    return (
        <div>
            <div className="mb-6">
                <h2 className="text-white text-xl font-semibold mb-2">Join requests ({requests.length})</h2>
                <p className="text-gray-400 text-sm bg-neutral-800 border border-neutral-700 rounded p-3">
                    These people have requested to join this Workspace. Adding new Workspace members will automatically update your bill. Workspace guests already count toward the free Workspace collaborator limit.
                </p>
            </div>

            {requests.length === 0 ? (
                <div className="text-center py-12">
                    <UserPlus size={48} className="text-gray-600 mx-auto mb-3" />
                    <p className="text-gray-400">No pending join requests</p>
                </div>
            ) : (
                <>
                    <div className="flex items-center justify-between mb-4">
                        <label className="flex items-center gap-2 text-gray-400 hover:text-white cursor-pointer">
                            <input
                                type="checkbox"
                                checked={selectedRequests.size === requests.length}
                                onChange={toggleAll}
                                className="w-4 h-4 rounded border-gray-600 bg-neutral-700 checked:bg-blue-500"
                            />
                            <span className="text-sm">Select all</span>
                        </label>

                        {selectedRequests.size > 0 && (
                            <div className="flex gap-3">
                                <button
                                    onClick={handleAddSelected}
                                    className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded text-sm flex items-center gap-2"
                                >
                                    <UserPlus size={16} />
                                    Add selected to workspace
                                </button>
                                <button
                                    onClick={handleDeleteSelected}
                                    className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded text-sm flex items-center gap-2"
                                >
                                    <UserX size={16} />
                                    Delete selected
                                </button>
                            </div>
                        )}
                    </div>

                    <div className="space-y-3">
                        {requests.map((request) => (
                            <div key={request.id} className="flex items-center gap-3 py-3 border-b border-neutral-700">
                                <input
                                    type="checkbox"
                                    checked={selectedRequests.has(request.id)}
                                    onChange={() => toggleRequest(request.id)}
                                    className="w-4 h-4 rounded border-gray-600 bg-neutral-700 checked:bg-blue-500"
                                />
                                <div className="w-10 h-10 rounded-full bg-gradient-to-br from-orange-500 to-red-500 flex items-center justify-center text-white font-semibold">
                                    {request.name.substring(0, 2).toUpperCase()}
                                </div>
                                <div className="flex-1">
                                    <div className="text-white font-medium">{request.name}</div>
                                    <div className="text-gray-400 text-sm">
                                        {request.username} • Requested {request.requestDate}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};

export default JoinRequestsTab;