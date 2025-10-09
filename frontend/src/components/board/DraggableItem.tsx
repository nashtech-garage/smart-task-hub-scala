import { detectUrl } from '@/utils/UrlPreviewUtils';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { X } from 'lucide-react';
import UrlPreview from './UrlPreview';
import { useAppDispatch, useAppSelector, type RootState } from '@/store';
import { useSelector } from 'react-redux';
import { selectTaskById } from '@/store/selectors/tasksSelectors';
import { showTaskModal } from '@/store/slices/taskModalSlice';
import { memo, useCallback } from 'react';
import { taskDeleted } from '@/store/slices/archiveTasksSlice';

interface DraggableItemProps {
    label?: string; // e.g., "FE", "BE"
    itemId: number;
}

const DraggableItem: React.FC<DraggableItemProps> = ({
    label,
    itemId
}) => {
    const item = useAppSelector((state) => selectTaskById(itemId)(state));
    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
        isDragging,
    } = useSortable({
        id: item.id,
        data: {
            type: 'item',
            item,
        },
    });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
    };

    const members = useSelector((state: RootState) => state.members);
    const dispatch = useAppDispatch();

    console.log("Rendering DraggableItem:", item.id, item.name);
    return (
        <div
            ref={setNodeRef}
            style={style}
            {...attributes}
            {...listeners}
            onClick={() => {
                detectUrl(item.name) ? undefined : dispatch(showTaskModal(item.id));
            }}
            className={`
                select-none bg-[#222f44] p-2 rounded-lg 
                shadow-sm cursor-grab hover:shadow-md   
                transition-shadow group 
                border border-transparent
                hover:border-white
                flex flex-col gap-1
                relative
                ${isDragging ? 'opacity-50' : ''}`}
        >
            {/* Label in top left corner */}
            {label && (
                <div className={`
                    self-start min-w-[56px] max-w-full h-[16px] px-2 rounded text-left text-xs font-medium text-white z-10
                    ${label === 'FE' ? 'bg-blue-600' : label === 'BE' ? 'bg-green-600' : 'bg-gray-600'}
                `}>
                    {label}
                </div>
            )}
            <button
                onClick={e => {
                    e.stopPropagation();
                    dispatch(taskDeleted(item.id));
                }}
                className='
                    opacity-0 group-hover:opacity-100 
                    transition-opacity text-gray-400 
                    hover:text-red-500 ml-2
                    self-end p-1 bg-white rounded-full
                    cursor-pointer z-10
                    absolute top-1 right-1
                    
                '
            >
                <X size={12} />
            </button>
            {/* URL Preview Display */}
            {detectUrl(item.name) ? (
                <div className='py-1 space-y-3 w-full'>
                    <UrlPreview
                        isDragging={isDragging}
                        url={item.name}
                        showRemoveButton={false}
                    />
                </div>
            ) : (
                <span className={`py-1 text-sm text-[#B6C2CF] flex-1 whitespace-pre-wrap`}>
                    {item.name}
                </span>
            )}

            {/* Assigned members in bottom right corner */}
            {item.memberIds && item.memberIds.length > 0 && (
                <div className="self-end flex -space-x-2 mt-1">
                    {item.memberIds.slice(0, 5).map(memberId => {
                        const member = members.find(m => m.id === memberId);
                        if (!member) return null;

                        return (
                            <div
                                key={member.id}
                                title={member.name}
                                className="w-6 h-6 rounded-full bg-gray-600 border-2 border-white shadow-sm flex items-center justify-center"
                            >
                                <span className="text-xs text-white font-medium">
                                    {member.name.charAt(0).toUpperCase()}
                                </span>
                            </div>
                        );
                    })}

                    {item.memberIds.length > 5 && (
                        <div className="w-6 h-6 rounded-full bg-gray-500 border-2 border-white shadow-sm flex items-center justify-center">
                            <span className="text-xs text-white font-medium">
                                +{item.memberIds.length - 5}
                            </span>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default memo(DraggableItem);