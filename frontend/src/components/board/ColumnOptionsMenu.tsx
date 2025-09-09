import React, { useEffect, useRef } from 'react';
import { Archive } from 'lucide-react';

interface ColumnOptionsMenuProps {
    isOpen: boolean;
    onClose: () => void;
    onArchiveColumn: () => void;
    onArchiveAllItems: () => void;
}

const ColumnOptionsMenu: React.FC<ColumnOptionsMenuProps> = ({
    isOpen,
    onClose,
    onArchiveColumn,
    onArchiveAllItems,
}) => {
    const menuRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!isOpen) return;

        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                onClose();
            }
        };

        const handleEscapeKey = (event: KeyboardEvent) => {
            if (event.key === 'Escape') {
                onClose();
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        document.addEventListener('keydown', handleEscapeKey);

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
            document.removeEventListener('keydown', handleEscapeKey);
        };
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    return (
        <div
            ref={menuRef}
            className="absolute top-full left-0 mt-1 w-64 bg-[#282E33] border border-[#394B59] rounded-lg shadow-lg z-50"
        >
            <div className="py-1">
                <div className="px-4 py-2 text-xs font-medium text-[#9FADBC] border-b border-[#394B59]">
                    List actions
                </div>
                
                <button
                    onClick={() => {
                        onArchiveColumn();
                        onClose();
                    }}
                    className="w-full px-4 py-2 text-left text-sm text-[#B6C2CF] hover:bg-[#394B59] transition-colors flex items-center gap-3"
                >
                    <Archive size={16} />
                    Archive this column
                </button>
                
                <button
                    onClick={() => {
                        onArchiveAllItems();
                        onClose();
                    }}
                    className="w-full px-4 py-2 text-left text-sm text-[#B6C2CF] hover:bg-[#394B59] transition-colors flex items-center gap-3"
                >
                    <Archive size={16} />
                    Archive all cards in this column
                </button>
            </div>
        </div>
    );
};

export default ColumnOptionsMenu;