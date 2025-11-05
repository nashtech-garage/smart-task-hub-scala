import type { Guest, JoinRequest, Member } from "@/types/collaboration";
import { UserPlus } from "lucide-react";
import { useState } from "react";
import MembersTab from "./member/MembersTab";
import GuestsTab from "./member/GuestsTab";
import JoinRequestsTab from "./member/JoinRequestsTab";
import InviteModal from "./member/InviteModal";

type TabType = 'members' | 'guests' | 'requests';

const sampleMembers: Member[] = [
    { id: '1', name: 'Ha.NguyenVan', username: '@hanguyenvan9', lastActive: 'November 2025', boardCount: 1, isAdmin: true },
    { id: '2', name: 'Long.LeVanQuoc', username: '@longlevanquoc1', lastActive: 'October 2025', boardCount: 1, isAdmin: true },
    { id: '3', name: 'Lâm Nhất Nguyên', username: '@nguyenlamnhat', lastActive: 'November 2025', boardCount: 2, isAdmin: true },
    { id: '4', name: 'Nhien Tran Duc', username: '@nhientranduc', lastActive: 'November 2025', boardCount: 1, isAdmin: true },
    { id: '5', name: 'Hiển Lương', username: '@tranhienluong2003', lastActive: 'November 2025', boardCount: 4, isAdmin: true },
    { id: '6', name: 'Vu Tran', username: '@vutran308', lastActive: 'November 2025', boardCount: 3, isAdmin: true },
    { id: '7', name: 'NguyenHuuTuan', username: '@tuungnhu12', lastActive: 'September 2025', boardCount: 1, isAdmin: true },
];

const sampleGuests: Guest[] = [];

const sampleRequests: JoinRequest[] = [];

const WorkspaceCollaborators: React.FC = () => {
    const [activeTab, setActiveTab] = useState<TabType>('members');
    const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
    const [members] = useState<Member[]>(sampleMembers);
    const [guests] = useState<Guest[]>(sampleGuests);
    const [requests] = useState<JoinRequest[]>(sampleRequests);

    const totalCollaborators = members.length + guests.length;

    return (
        <div className="h-full bg-[#1F1F21] text-white p-8">
            <div className="w-full">
                <div className="flex justify-between items-center mb-6">
                    <h1 className="text-xl font-bold">
                        Collaborators <span className="text-gray-400">{totalCollaborators} / 10</span>
                    </h1>
                    <button
                        onClick={() => setIsInviteModalOpen(true)}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded flex items-center gap-2"
                    >
                        <UserPlus size={18} />
                        Invite Workspace members
                    </button>
                </div>

                <div className="flex gap-6">
                    <div className="w-64 space-y-2">
                        <button
                            onClick={() => setActiveTab('members')}
                            className={`cursor-pointer w-full text-left px-4 py-2 rounded ${activeTab === 'members' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-neutral-800'
                                }`}
                        >
                            Workspace members ({members.length})
                        </button>
                        <button
                            onClick={() => setActiveTab('guests')}
                            className={`cursor-pointer w-full text-left px-4 py-2 rounded ${activeTab === 'guests' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-neutral-800'
                                }`}
                        >
                            Guests ({guests.length})
                        </button>
                        <button
                            onClick={() => setActiveTab('requests')}
                            className={`cursor-pointer w-full text-left px-4 py-2 rounded ${activeTab === 'requests' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-neutral-800'
                                }`}
                        >
                            Join requests ({requests.length})
                        </button>
                    </div>

                    <div className="flex-1">
                        {activeTab === 'members' && <MembersTab members={members} />}
                        {activeTab === 'guests' && <GuestsTab guests={guests} />}
                        {activeTab === 'requests' && <JoinRequestsTab requests={requests} />}
                    </div>
                </div>
            </div>

            <InviteModal isOpen={isInviteModalOpen} onClose={() => setIsInviteModalOpen(false)} />
        </div>
    );
};

export default WorkspaceCollaborators;