import React, { useEffect, useState } from 'react';
import { X, Calendar, Users,  
  List, Edit3, Archive, 
  // Eye, Paperclip, Copy, Trash2, MessageSquareText 
} from 'lucide-react';
import LoadingContent from '../ui/LoadingContent';

interface TaskModalProps {
  onClose: () => void;
  item: {
    id: string;
    content: string;
    columnId?: string;
  };
  onUpdate?: (itemId: string, updates: any) => void;
  onDelete?: (itemId: string) => void;
}

const TaskDetailModal: React.FC<TaskModalProps> = ({ 
  onClose, 
  item, 
  onUpdate, 
  // onDelete 
}) => {
  const [isLoading, setIsLoading] = useState(false);
  const [title, setTitle] = useState(item.content);
  const [description, setDescription] = useState("As a workgroup member, I want to create, assign, update, and delete tasks inside a project, so that we can manage our work effectively in a collaborative environment.");
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [isEditingDescription, setIsEditingDescription] = useState(false);
  // const [comment, setComment] = useState('');

  const handleSaveTitle = () => {
    setIsEditingTitle(false);
    onUpdate?.(item.id, { content: title });
  };

  // const handleDeleteTask = () => {
  //   if (window.confirm('Are you sure you want to delete this task?')) {
  //     onDelete?.(item.id);
  //     onClose();
  //   }
  // };

  useEffect(() => {
    setIsLoading(true);
      setTimeout(() => {
      return setIsLoading(false)
    }, 2000)
  }, []);

  return (
    <div className="fixed inset-0 bg-black/50 flex items-start justify-center z-50 pt-8 px-4">
      <div className="bg-[#282e3e] rounded-lg w-full max-w-3xl max-h-[calc(100vh-4rem)] overflow-hidden flex shadow-2xl">
        {
          isLoading ?
          <div className="flex-1 flex items-center justify-center min-h-[300px]">
            <LoadingContent />
          </div> : 
          <>
            {/* Main Content */}
            <div className="flex-1 overflow-y-auto">
              {/* Header */}
              <div className="p-6 pb-4">
                <div className="flex items-start justify-between mb-2">
                  {isEditingTitle ? (
                    <input
                      type="text"
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      onBlur={handleSaveTitle}
                      onKeyDown={(e) => e.key === 'Enter' && handleSaveTitle()}
                      className="text-xl font-medium text-white bg-transparent border-b border-blue-500 outline-none flex-1 mr-4"
                      autoFocus
                    />
                  ) : (
                    <h1 
                      className="text-xl font-medium text-white cursor-pointer hover:bg-gray-700 hover:bg-opacity-50 p-1 rounded flex-1 mr-4"
                      onClick={() => setIsEditingTitle(true)}
                    >
                      [Sprint X][Tasks][BE] {title}
                    </h1>
                  )}
                  <button
                    onClick={onClose}
                    className="text-gray-400 hover:text-white p-1 rounded hover:bg-gray-600 hover:bg-opacity-50"
                  >
                    <X size={18} />
                  </button>
                </div>
                <div className="text-sm text-gray-400 mb-4">
                  in list <span className="underline cursor-pointer">Backlog</span>
                </div>

                {/* Action Buttons */}
                <div className="flex flex-wrap gap-2">
                  <button className="flex items-center gap-2 px-3 py-2 bg-gray-600 bg-opacity-50 hover:bg-opacity-70 text-white text-sm rounded">
                    <Calendar size={14} />
                    Dates
                  </button>
                  <button className="flex items-center gap-2 px-3 py-2 bg-gray-600 bg-opacity-50 hover:bg-opacity-70 text-white text-sm rounded">
                    <List size={14} />
                    Checklist
                  </button>
                  <button className="flex items-center gap-2 px-3 py-2 bg-gray-600 bg-opacity-50 hover:bg-opacity-70 text-white text-sm rounded">
                    <Users size={14} />
                    Members
                  </button>
                </div>
              </div>

              {/* Labels Section */}
              <div className="px-6 pb-4">
                <h3 className="text-sm font-medium text-white mb-2">Labels</h3>
                <div className="flex items-center gap-2 mb-1">
                  <span className="px-2 py-1 bg-green-600 text-white text-xs font-medium rounded">BE</span>
                  <button className="w-6 h-6 bg-gray-600 bg-opacity-50 hover:bg-opacity-70 text-gray-300 text-lg rounded flex items-center justify-center leading-none">+</button>
                </div>
                <div className="text-xs text-gray-500 mt-1">Story Points</div>
              </div>

              {/* Description Section */}
              <div className="px-6 pb-4">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="text-lg font-medium text-white flex items-center gap-2">
                    <List size={18} />
                    Description
                  </h3>
                  <button 
                    onClick={() => setIsEditingDescription(!isEditingDescription)}
                    className="text-gray-400 hover:text-white p-1.5 hover:bg-gray-600 hover:bg-opacity-50 rounded"
                  >
                    <Edit3 size={16} />
                  </button>
                </div>
                
                {isEditingDescription ? (
                  <div>
                    <textarea
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      className="w-full h-32 p-3 bg-[#22272b] text-white rounded text-sm resize-none outline-none border border-gray-600 focus:border-blue-500"
                      placeholder="Add a more detailed description..."
                    />
                    <div className="flex gap-2 mt-3">
                      <button 
                        onClick={() => setIsEditingDescription(false)}
                        className="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white text-sm rounded"
                      >
                        Save
                      </button>
                      <button 
                        onClick={() => {
                          setIsEditingDescription(false);
                        }}
                        className="px-3 py-1.5 bg-transparent hover:bg-gray-600 hover:bg-opacity-50 text-gray-300 text-sm rounded"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                ) : (
                  <div 
                    onClick={() => setIsEditingDescription(true)}
                    className="bg-[#22272b] p-3 rounded text-gray-300 text-sm leading-relaxed cursor-pointer hover:bg-opacity-80"
                  >
                    {description || "Add a more detailed description..."}
                  </div>
                )}
              </div>

              {/* Activity Section */}
              <div className="px-6 pb-6">
                {/* <h3 className="text-lg font-medium text-white mb-4 flex items-center gap-2">
                  <MessageSquareText />
                  Comments
                </h3> */}
                
                {/* Comment Input */}
                {/* <div className="flex gap-3 mb-4">
                  <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white text-sm font-medium flex-shrink-0">
                    A
                  </div>
                  <div className="flex-1">
                    <textarea
                      value={comment}
                      onChange={(e) => setComment(e.target.value)}
                      placeholder="Write a comment..."
                      className="w-full p-3 bg-[#22272b] text-white rounded text-sm resize-none outline-none border border-gray-600 focus:border-blue-500"
                      rows={3}
                    />
                    {comment && (
                      <div className="flex gap-2 mt-2">
                        <button 
                          onClick={() => setComment('')}
                          className="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white text-sm rounded"
                        >
                          Save
                        </button>
                      </div>
                    )}
                  </div>
                </div> */}

                {/* Activity Item */}
                {/* <div className="flex gap-3 text-sm">
                  <div className="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center text-white text-xs font-medium flex-shrink-0">
                    A
                  </div>
                  <div className="flex-1">
                    <div className="text-gray-300">
                      <span className="font-medium text-white">ABC</span> copied this card from{' '}
                      <span className="text-blue-400 underline cursor-pointer hover:text-blue-300">
                        Create, assign, update, and delete tasks
                      </span>{' '}
                      in list Backlog
                    </div>
                    <div className="text-xs text-gray-500 mt-1">Jul 24, 2023, 3:19 PM</div>
                  </div>
                </div> */}
              </div>
            </div>

            {/* Right Sidebar */}
            <div className="w-48 bg-[#282e3e] border-l border-gray-600 border-opacity-50">
              <div className="p-4">
                {/* Add to Card */}
                <div className="mb-6">
                  <h4 className="text-xs font-medium text-gray-400 mb-3 uppercase tracking-wide">
                    ADD TO CARD
                  </h4>
                  <div className="space-y-1">
                    <button className="w-full text-left px-2 py-2 text-sm text-gray-300 hover:bg-gray-600 hover:bg-opacity-50 rounded flex items-center gap-2">
                      <Users size={14} />
                      Members
                    </button>
                    <button className="w-full text-left px-2 py-2 text-sm text-gray-300 hover:bg-gray-600 hover:bg-opacity-50 rounded flex items-center gap-2">
                      <div className="w-3.5 h-3.5 bg-gray-500 rounded"></div>
                      Labels
                    </button>
                    <button className="w-full text-left px-2 py-2 text-sm text-gray-300 hover:bg-gray-600 hover:bg-opacity-50 rounded flex items-center gap-2">
                      <List size={14} />
                      Checklist
                    </button>
                    <button className="w-full text-left px-2 py-2 text-sm text-gray-300 hover:bg-gray-600 hover:bg-opacity-50 rounded flex items-center gap-2">
                      <Calendar size={14} />
                      Dates
                    </button>
                  </div>
                </div>

                {/* Actions */}
                <div>
                  <h4 className="text-xs font-medium text-gray-400 mb-3 uppercase tracking-wide">
                    ACTIONS
                  </h4>
                  <div className="space-y-1">
                    <button className="w-full text-left px-2 py-2 text-sm text-gray-300 hover:bg-gray-600 hover:bg-opacity-50 rounded flex items-center gap-2">
                      <Archive size={14} />
                      Archive
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </>
        }
      </div>
    </div>
  );
}

export default TaskDetailModal;