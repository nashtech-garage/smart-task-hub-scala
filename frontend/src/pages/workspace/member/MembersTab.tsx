import type { Member } from "@/types/collaboration";
import { Link2, X } from "lucide-react";

const MembersTab: React.FC<{ members: Member[] }> = ({ members }) => {
    return (
        <div>
            <div className="mb-6">
                <h2 className="text-white text-xl font-semibold mb-2">Workspace members ({members.length})</h2>
                <p className="text-gray-400 text-sm">
                    Workspace members can view and join all Workspace visible boards and create new boards in the Workspace.
                </p>
            </div>

            <div className="mb-6">
                <h3 className="text-white text-lg font-semibold mb-3">Invite members to join you</h3>
                <p className="text-gray-400 text-sm mb-4">
                    Anyone with an invite link can join this free Workspace. You can also disable and create a new invite link for this Workspace at any time. Pending invitations count toward the 10 collaborator limit.
                </p>
                <div className="flex gap-3 mb-4">
                    <button className="text-gray-400 hover:text-white text-sm">Disable invite link</button>
                    <button className="text-blue-400 hover:text-blue-300 text-sm flex items-center gap-2">
                        <Link2 size={16} />
                        Invite with link
                    </button>
                </div>

                <input
                    type="text"
                    placeholder="Filter by name"
                    className="w-full max-w-sm bg-neutral-800 border border-neutral-700 rounded px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                />
            </div>

            <div className="space-y-3">
                {members.map((member) => (
                    <div key={member.id} className="flex items-center justify-between py-3 border-b border-neutral-700">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white font-semibold">
                                {member.name.substring(0, 2).toUpperCase()}
                            </div>
                            <div>
                                <div className="text-white font-medium">{member.name}</div>
                                <div className="text-gray-400 text-sm">
                                    {member.username} • Last active {member.lastActive}
                                </div>
                            </div>
                        </div>
                        <div className="flex items-center gap-4">
                            <button className="text-gray-400 hover:text-white text-sm">
                                View boards ({member.boardCount})
                            </button>
                            <button className="text-gray-400 hover:text-white text-sm flex items-center gap-1">
                                Admin
                                <span className="text-gray-600">ⓘ</span>
                            </button>
                            <button className="text-gray-400 hover:text-white">
                                <X size={18} />
                            </button>
                            <button className="text-gray-400 hover:text-white text-sm">Remove...</button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default MembersTab;