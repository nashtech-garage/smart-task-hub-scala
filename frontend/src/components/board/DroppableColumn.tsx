import type { Column, Item } from '@/types';
import {
    SortableContext,
    useSortable,
    verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Ellipsis, Plus, X } from 'lucide-react';
import React, {
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';
import UrlPreview from './UrlPreview';
import { detectUrl } from '@/utils/UrlPreviewUtils';
import DraggableItem from './DraggableItem';
import ColumnOptionsMenu from './ColumnOptionsMenu';

interface DroppableColumnProps {
    column: Column;
    items: Item[];
    isAddingCard: boolean;
    cardTitle: string;
    setCardTitle: (title: string) => void;
    onStartAddingCard: (columnId: number) => void;
    onSubmitCard: (columnId: number) => void;
    onCancelCard: () => void;
    onDeleteItem: (itemId: number) => void;
    onDeleteColumn: (columnId: number) => void;
    onUpdateColumnTitle: (columnId: number, newTitle: string) => void;
    onArchiveColumn: (columnId: number) => void;
    onArchiveAllItems: (columnId: number) => void;
    handleShowDetailTask: () => void;
}

const DroppableColumnComponent: React.FC<DroppableColumnProps> = ({
    column,
    items,
    isAddingCard,
    cardTitle,
    setCardTitle,
    onStartAddingCard,
    onSubmitCard,
    onCancelCard,
    onDeleteItem,
    onUpdateColumnTitle,
    onArchiveColumn,
    onArchiveAllItems,
    handleShowDetailTask
}) => {
    const [isEditingTitle, setIsEditingTitle] = useState(false);
    const [showOptionsMenu, setShowOptionsMenu] = useState(false);
    const [titleValue, setTitleValue] = useState(column.name);
    const [detectedUrl, setDetectedUrl] = useState<string | null>(null);
    const inputRef = useRef<HTMLTextAreaElement>(null);
    const titleInputRef = useRef<HTMLInputElement>(null);
    const columnRef = useRef<HTMLDivElement>(null);

    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
        isDragging,
    } = useSortable({
        id: column.id,
        data: {
            type: 'column',
            column,
        },
    });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
    };

    const itemIds = useMemo(() => items.map(item => item.id), [items]);

    useEffect(() => {
        if (isAddingCard && inputRef.current) {
            inputRef.current.focus();
        }
    }, [isAddingCard]);

    useEffect(() => {
        if (isEditingTitle && titleInputRef.current) {
            titleInputRef.current.focus();
            titleInputRef.current.select();
        }
    }, [isEditingTitle]);

    // Detect URLs in card title
    const memorizedDetectedUrl = useMemo(() => {
        return cardTitle ? detectUrl(cardTitle) : null;
    }, [cardTitle]);

    useEffect(() => {
        setDetectedUrl(memorizedDetectedUrl);
    }, [memorizedDetectedUrl]);

    useEffect(() => {
        if (!isEditingTitle) return;

        const handleClickOutside = (event: MouseEvent) => {
            if (
                isEditingTitle &&
                columnRef.current &&
                !columnRef.current.contains(event.target as Node)
            ) {
                handleTitleSubmit();
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isEditingTitle]);

    const handleAddCard = useCallback(() => {
        // Close title editing and options menu when starting to add card
        if (isEditingTitle) {
            handleTitleSubmit();
        }
        if (showOptionsMenu) {
            setShowOptionsMenu(false);
        }
        onStartAddingCard(column.id);
    }, [isEditingTitle, showOptionsMenu, column.id, onStartAddingCard]);

    const handleSubmit = useCallback(() => {
        onSubmitCard(column.id);
        setDetectedUrl(null);
    }, [column.id, onSubmitCard]);

    const handleKeyDown = useCallback(
        (e: React.KeyboardEvent) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSubmit();
            } else if (e.key === 'Escape') {
                onCancelCard();
            }
        },
        [handleSubmit, onCancelCard]
    );

    const handleTitleClick = useCallback(() => {
        // Close add card input and options menu if they're open
        if (isAddingCard) {
            onCancelCard();
        }
        if (showOptionsMenu) {
            setShowOptionsMenu(false);
        }
        setIsEditingTitle(true);
    }, [isAddingCard, showOptionsMenu, onCancelCard]);

    const handleTitleSubmit = useCallback(() => {
        if (titleValue.trim() && titleValue.trim() !== column.name) {
            onUpdateColumnTitle(column.id, titleValue.trim());
        } else {
            setTitleValue(column.name); // Reset to original if empty or unchanged
        }
        setIsEditingTitle(false);
    }, [titleValue, column.name, column.id, onUpdateColumnTitle]);

    const handleTitleKeyDown = useCallback(
        (e: React.KeyboardEvent) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                handleTitleSubmit();
            } else if (e.key === 'Escape') {
                setTitleValue(column.name); // Reset to original
                setIsEditingTitle(false);
            }
        },
        [handleTitleSubmit, column.name]
    );

    const handleRemoveUrlPreview = useCallback(() => {
        setDetectedUrl(null);
        // Optionally remove the URL from the card title
        if (detectedUrl) {
            const newTitle = cardTitle.replace(detectedUrl, '').trim();
            setCardTitle(newTitle);
        }
    }, [detectedUrl, cardTitle, setCardTitle]);

    // Prevent event bubbling to parent container when clicking inside the input area
    const handleInputAreaClick = useCallback((e: React.MouseEvent) => {
        e.stopPropagation();
    }, []);

    const handleOptionsMenuToggle = useCallback(
        (e: React.MouseEvent) => {
            e.stopPropagation();
            // Close other UI states when opening options menu
            if (isAddingCard) {
                onCancelCard();
            }
            if (isEditingTitle) {
                handleTitleSubmit();
            }
            setShowOptionsMenu(!showOptionsMenu);
        },
        [showOptionsMenu, isAddingCard, isEditingTitle, onCancelCard, handleTitleSubmit]
    );

    const handleTitleInputClick = useCallback((e: React.MouseEvent) => {
        e.stopPropagation();
    }, []);

    const handleTitleDisplay = useCallback(
        (e: React.MouseEvent) => {
            e.stopPropagation();
            handleTitleClick();
        },
        [handleTitleClick]
    );

    const handleArchiveColumn = useCallback(() => {
        onArchiveColumn(column.id);
    }, [column.id, onArchiveColumn]);

    const handleArchiveAllItems = useCallback(() => {
        onArchiveAllItems(column.id);
    }, [column.id, onArchiveAllItems]);

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={`select-none bg-[#101204] rounded-xl p-2 pr-1 w-[272px] flex-shrink-0 
                max-h-full flex flex-col
                ${isDragging ? 'opacity-50' : ''}`}
        >
            <div
                ref={columnRef}
                {...attributes}
                {...listeners}
                className='flex-shrink-0 flex items-center justify-between gap-4 cursor-grab p-2 pb-3'
            >
                <div className='flex items-center gap-2 flex-1'>
                    {isEditingTitle ? (
                        <input
                            ref={titleInputRef}
                            value={titleValue}
                            onChange={e => setTitleValue(e.target.value)}
                            onKeyDown={handleTitleKeyDown}
                            onBlur={handleTitleSubmit}
                            className='font-semibold text-[#B6C2CF] bg-[#22272B] border border-[#394B59] rounded px-2 py-1 -mx-2 -my-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent flex-1'
                            onClick={handleTitleInputClick} // Prevent drag when clicking input
                        />
                    ) : (
                        <h3
                            className='font-semibold text-[#B6C2CF] cursor-pointer border border-transparent hover:bg-[#22272B] rounded px-2 py-1 -mx-2 -my-1 transition-colors flex-1'
                            onClick={handleTitleDisplay}
                        >
                            {column.name}
                        </h3>
                    )}
                </div>
                <div className="relative">
                    <button
                        onClick={handleOptionsMenuToggle}
                        className='text-gray-400 hover:text-gray-300 hover:bg-[#22272B] rounded p-1 transition-colors flex-shrink-0'
                    >
                        <Ellipsis size={16} />
                    </button>
                    
                    <ColumnOptionsMenu
                        isOpen={showOptionsMenu}
                        onClose={() => setShowOptionsMenu(false)}
                        onArchiveColumn={handleArchiveColumn}
                        onArchiveAllItems={handleArchiveAllItems}
                    />
                </div>
            </div>
            <SortableContext
                items={itemIds}
                strategy={verticalListSortingStrategy}
            >
                <div className='overflow-y-auto space-y-3 pr-1'>
                    {items.map(item => (
                        <DraggableItem
                            key={item.id}
                            item={item}
                            onDelete={onDeleteItem}
                            handleShowDetailTask={handleShowDetailTask}
                            // label='FE'
                            // assignedMember={{ 
                            //     name: "John Doe", 
                            //     initials: "JD",
                            // }}
                        />
                    ))}
                </div>
            </SortableContext>

            <div className='flex-shrink-0 mt-3 pr-1'>
                {isAddingCard ? (
                    <div className='space-y-2' onClick={handleInputAreaClick}>
                        {detectedUrl && (
                            <UrlPreview
                                url={detectedUrl}
                                onRemove={handleRemoveUrlPreview}
                                showRemoveButton={true}
                            />
                        )}

                        <textarea
                            ref={inputRef}
                            value={cardTitle}
                            onChange={e => setCardTitle(e.target.value)}
                            onKeyDown={handleKeyDown}
                            placeholder='Enter a title or paste a link'
                            className='w-full p-2 text-sm bg-[#22272B] text-[#B6C2CF] border border-[#394B59] rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent'
                            rows={3}
                        />
                        <div className='flex items-center gap-2'>
                            <button
                                onClick={handleSubmit}
                                disabled={!cardTitle.trim()}
                                className='px-3 py-1.5 text-sm bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white rounded transition-colors'
                            >
                                Add card
                            </button>
                            <button
                                onClick={onCancelCard}
                                className='p-1.5 text-gray-400 hover:text-gray-300 hover:bg-[#22272B] rounded transition-colors'
                            >
                                <X size={16} />
                            </button>
                        </div>
                    </div>
                ) : (
                    <button
                        onClick={handleAddCard}
                        className='w-full p-2 text-[#B6C2CF] hover:text-[#B6C2CF] hover:bg-[#222f44] rounded-lg transition-colors flex items-center gap-2'
                    >
                        <Plus size={16} />
                        Add a card
                    </button>
                )}
            </div>
        </div>
    );
};

// Custom comparison function for React.memo
const arePropsEqual = (
    prevProps: DroppableColumnProps,
    nextProps: DroppableColumnProps
) => {
    // Quick checks for primitive values
    if (
        prevProps.isAddingCard !== nextProps.isAddingCard ||
        prevProps.cardTitle !== nextProps.cardTitle ||
        prevProps.column.id !== nextProps.column.id ||
        prevProps.column.name !== nextProps.column.name
    ) {
        return false;
    }

    // Check items array
    if (prevProps.items.length !== nextProps.items.length) {
        return false;
    }

    // Compare each item
    for (let i = 0; i < prevProps.items.length; i++) {
        const prevItem = prevProps.items[i];
        const nextItem = nextProps.items[i];

        if (
            prevItem.id !== nextItem.id ||
            prevItem.content !== nextItem.content
        ) {
            return false;
        }
    }

    // If all checks pass, props are equal
    return true;
};

const DroppableColumn = React.memo(DroppableColumnComponent, arePropsEqual);

DroppableColumn.displayName = 'DroppableColumn';

export default DroppableColumn;