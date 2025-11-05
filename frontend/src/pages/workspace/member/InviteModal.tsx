import { Link2, X } from "lucide-react";
import { useState } from "react";

const InviteModal: React.FC<{ isOpen: boolean; onClose: () => void }> = ({ isOpen, onClose }) => {
    const [inviteLink] = useState('https://workspace.example.com/invite/abc123');
    const [copied, setCopied] = useState(false);

    if (!isOpen) return null;

    const handleCopyLink = () => {
        navigator.clipboard.writeText(inviteLink);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-neutral-800 rounded-lg w-full max-w-md p-6">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-white text-lg font-semibold">Invite to Workspace</h2>
                    <button onClick={onClose} className="text-gray-400 hover:text-white">
                        <X size={20} />
                    </button>
                </div>

                <input
                    type="text"
                    placeholder="Email address or name"
                    className="w-full bg-neutral-700 border border-neutral-600 rounded px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 mb-4"
                />

                <div className="mb-2">
                    <p className="text-gray-400 text-sm mb-1">Invite someone to this Workspace with a link:</p>
                    <button
                        onClick={handleCopyLink}
                        className="text-blue-400 hover:text-blue-300 text-sm flex items-center gap-2"
                    >
                        <Link2 size={16} />
                        {copied ? 'Link copied!' : 'Copy link'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default InviteModal;