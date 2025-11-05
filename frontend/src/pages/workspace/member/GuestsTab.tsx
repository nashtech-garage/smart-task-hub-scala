import type { Guest } from "@/types/collaboration";
import { Users, X } from "lucide-react";

const GuestsTab: React.FC<{ guests: Guest[] }> = ({ guests }) => {
    return (
        <div>
            <div className="mb-6">
                <h2 className="text-white text-xl font-semibold mb-2">Guests ({guests.length})</h2>
                <p className="text-gray-400 text-sm bg-neutral-800 border border-neutral-700 rounded p-3">
                    Guests can only view and edit the boards to which they've been added.
                </p>
            </div>

            {guests.length === 0 ? (
                <div className="text-center py-12">
                    <Users size={48} className="text-gray-600 mx-auto mb-3" />
                    <p className="text-gray-400">No guests in this workspace</p>
                </div>
            ) : (
                <div className="space-y-3">
                    {guests.map((guest) => (
                        <div key={guest.id} className="flex items-center justify-between py-3 border-b border-neutral-700">
                            <div className="flex items-center gap-3">
                                <div className="w-10 h-10 rounded-full bg-gradient-to-br from-green-500 to-teal-500 flex items-center justify-center text-white font-semibold">
                                    {guest.name.substring(0, 2).toUpperCase()}
                                </div>
                                <div>
                                    <div className="text-white font-medium">{guest.name}</div>
                                    <div className="text-gray-400 text-sm">
                                        {guest.username} • Last active {guest.lastActive}
                                    </div>
                                </div>
                            </div>
                            <div className="flex items-center gap-4">
                                <button className="text-gray-400 hover:text-white text-sm">
                                    View boards ({guest.boardCount})
                                </button>
                                <button className="text-gray-400 hover:text-white">
                                    <X size={18} />
                                </button>
                                <button className="text-gray-400 hover:text-white text-sm">Remove...</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default GuestsTab;