import { fetchBoardMembers } from "@/services/boardService";
import taskService from "@/services/taskService";
import { notify } from "@/services/toastService";
import type { Member } from "@/types";
import React, { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useParams } from "react-router-dom";

interface AssignMembersProps {
    assignedMembers?: Member[];
    taskId: number;
}

const AssignMembers: React.FC<AssignMembersProps> = (props) => {
    const { boardId } = useParams();
    const [assignedMembers, setAssignedMembers] = useState<Member[]>(props?.assignedMembers || []);
    const [members, setMembers] = useState<Member[]>([])
    const [isOpen, setIsOpen] = useState(false);
    const [search, setSearch] = useState("");
    const buttonRef = useRef<HTMLButtonElement>(null);
    const popupRef = useRef<HTMLDivElement>(null);
    const [coords, setCoords] = useState<{ top: number; left: number } | null>(null);

    const fetchMemebers = async () => {
        try {
            const membersData = await fetchBoardMembers(Number(boardId));
            setMembers(membersData.data);
        } catch (error: any) {
            notify.error(error.response?.data?.message);
        }
    }

    const toggleAssign = async (member: Member) => {
        try {
            const isAssigned = assignedMembers.some(am => am.id === member.id);

            if (isAssigned) {
                // remove
                await taskService.removeMember(Number(boardId), props.taskId, member.id);
                setAssignedMembers(prev => prev.filter(am => am.id !== member.id));
                notify.success(`${member.name} removed`);
            } else {
                // assign
                await taskService.assignMember(Number(boardId), props.taskId, member.id);
                setAssignedMembers(prev => [...prev, member]);
                notify.success(`${member.name} assigned`);
            }
        } catch (error: any) {
            notify.error(error.response?.data?.message || "Something went wrong");
        }
    };

    useEffect(() => {
        fetchMemebers();
    }, [boardId]);

    const filtered = members.filter((m) =>
        m.name.toLowerCase().includes(search.toLowerCase())
    );

    // Calculate popup position
    useEffect(() => {
        if (isOpen && buttonRef.current) {
            const rect = buttonRef.current.getBoundingClientRect();
            setCoords({
                top: rect.bottom + window.scrollY,
                left: rect.left + window.scrollX,
            });
        }
    }, [isOpen]);

    // turn off popup when clicking outside
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (
                popupRef.current &&
                !popupRef.current.contains(event.target as Node) &&
                buttonRef.current &&
                !buttonRef.current.contains(event.target as Node)
            ) {
                setIsOpen(false);
            }
        }

        if (isOpen) {
            document.addEventListener("mousedown", handleClickOutside);
        } else {
            document.removeEventListener("mousedown", handleClickOutside);
        }

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [isOpen]);

    return (

        <div className="pr-2 pb-4">
            <h3 className="text-sm font-medium text-[var(--foreground)] mb-2">Members</h3>
            <div className="flex items-center gap-1 mb-1">
                {
                    assignedMembers?.map((assignedMember) => (
                        <div key={assignedMember.id} title={assignedMember.name} className='w-8 h-8 rounded-full bg-[#282e3e] border-1 border-gray-400 shadow-sm flex items-center justify-center'>
                            <span className='text-xs text-white font-medium'>
                                {assignedMember.name.charAt(0).toUpperCase()}
                            </span>
                        </div>
                    ))
                }

                <button
                    ref={buttonRef}
                    onClick={() => setIsOpen(!isOpen)}
                    className="w-8 h-8 bg-gray-600 bg-opacity-50 hover:bg-opacity-70 
                   text-gray-300 text-lg rounded-full flex items-center 
                   justify-center leading-none"
                >
                    +
                </button>

                {/* Popup render outside by Portal */}
                {isOpen &&
                    coords &&
                    createPortal(
                        <div
                            ref={popupRef}
                            style={{
                                position: "absolute",
                                top: coords.top,
                                left: coords.left,
                                zIndex: 9999,
                            }}
                            className="border border-gray-600 bg-[var(--background)] w-64 rounded-lg shadow-2xl p-4 mt-2"
                        >
                            {/* Header */}
                            <div className="flex justify-between items-center border-b pb-2">
                                <h2 className="text-lg text-[var(--foreground)] font-semibold">Assign Members</h2>
                                <button
                                    onClick={() => setIsOpen(false)}
                                    className="text-gray-400 hover:text-[var(--foreground)]"
                                >
                                    ✕
                                </button>
                            </div>

                            {/* Search */}
                            <input
                                type="text"
                                placeholder="Search members..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className="w-full text-gray-400 border rounded px-2 py-1 mt-3 mb-3 bg-[var(--background)] border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />

                            {/* List members */}
                            <ul className="space-y-2 max-h-60 overflow-y-auto">
                                {/* Assigned Members */}
                                {filtered.filter(m => assignedMembers.some(am => am.id === m.id)).length > 0 && (
                                    <>
                                        <li className="text-gray-500 text-xs font-semibold px-2">Assigned</li>
                                        {filtered
                                            .filter(m => assignedMembers.some(am => am.id === m.id))
                                            .map(m => (
                                                <li
                                                    key={m.id}
                                                    onClick={() => toggleAssign(m)}
                                                    className="flex items-center justify-between px-2 py-1 rounded bg-[var(--background)] cursor-pointer hover:bg-[var(--hover-bg)]"
                                                >
                                                    <span className="text-[var(--foreground)]">{m.name}</span>
                                                    <span className="text-green-400 text-xs">✓</span>
                                                </li>
                                            ))}
                                    </>
                                )}

                                {/* Unassigned Members */}
                                {filtered.filter(m => !assignedMembers.some(am => am.id === m.id)).length > 0 && (
                                    <>
                                        <li className="text-gray-500 text-xs font-semibold px-2 mt-2">Unassigned</li>
                                        {filtered
                                            .filter(m => !assignedMembers.some(am => am.id === m.id))
                                            .map(m => (
                                                <li
                                                    key={m.id}
                                                    onClick={() => toggleAssign(m)}
                                                    className="flex items-center justify-between px-2 py-1 rounded hover:bg-[var(--hover-bg)] cursor-pointer"
                                                >
                                                    <span className="text-[var(--foreground)]">{m.name}</span>
                                                </li>
                                            ))}
                                    </>
                                )}

                                {/* Empty state */}
                                {filtered.length === 0 && (
                                    <li className="text-gray-400 text-sm text-center">
                                        No members found
                                    </li>
                                )}
                            </ul>
                        </div>,
                        document.getElementById("portal")!
                    )}

            </div>
        </div>

    );
};

export default AssignMembers;
