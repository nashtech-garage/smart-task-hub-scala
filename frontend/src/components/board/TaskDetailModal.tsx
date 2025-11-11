import React, { useCallback, useEffect, useState } from 'react';
import {
  X, Calendar, Users,
  List, Edit3, Archive,
  // Eye, Paperclip, Copy, Trash2, MessageSquareText 
} from 'lucide-react';
import LoadingContent from '../ui/LoadingContent';
import type { ItemDetail, UpdateItemRequest } from '@/types';
import taskService from '@/services/taskService';
import AssignMembers from './AssignMembers';
import { hideTaskModal } from '@/store/slices/taskModalSlice';
import { useAppDispatch } from '@/store';
import { useSelector } from 'react-redux';
import { selectTaskModalState } from '@/store/selectors/modalSelector';
import { notify } from '@/services/toastService';

const TaskDetailModal: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [item, setItem] = useState<ItemDetail>(null as any);
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [isEditingDescription, setIsEditingDescription] = useState(false);
  // const [comment, setComment] = useState('');

  const dispatch = useAppDispatch();
  const { taskId } = useSelector(selectTaskModalState);

  const onModalClose = useCallback(() => {
    dispatch(hideTaskModal());
  }, [dispatch]);

  const archiveTask = useCallback(
    async (taskId: number) => {
      try {
        const result = await taskService.archiveTask(taskId);
        notify.success(result.message);
      } catch (error: any) {
        notify.error(error.response?.data?.message || 'Failed to archive task');
      }
    },
    []
  );

  const updateTask = useCallback(
    async (taskId: number, data: UpdateItemRequest) => {
      try {
        const result = await taskService.updateTask(taskId, data);
        notify.success(result.message);
      } catch (error: any) {
        notify.error(error.response?.data?.message || 'Failed to update task');
      }
    },
    []
  );

  const handleUpdate = () => {
    setIsEditingTitle(false);
    updateTask(item.id, { name: item.name, description: item.description as string });
  };

  const fetchTaskDetail = async () => {
    setIsLoading(true);
    try {
      const response = await taskService.getTaskDetail(Number(taskId));
      setItem(response.data);
    }
    catch (error) {
      console.error('Failed to fetch task details:', error);
    }
    finally {
      setIsLoading(false);
    }
  };

  // Close modal on Escape key press
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") onModalClose();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [onModalClose]);

  // console.log("Rendering TaskDetailModal for taskId:", taskId, item);

  useEffect(() => {
    fetchTaskDetail();
  }, [taskId]);

  return (
    <div className="fixed inset-0 bg-black/50 flex items-start justify-center z-40 pt-8 px-4">
      <div className="bg-[var(--background)] rounded-lg w-full max-w-3xl max-h-[calc(100vh-4rem)] flex shadow-2xl">
        {
          isLoading ?
            <div className="flex-1 flex items-center justify-center min-h-[300px] overflow-y-auto">
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
                        value={item.name}
                        onChange={(e) => setItem({ ...item, name: e.target.value })}
                        onBlur={handleUpdate}
                        onKeyDown={(e) => e.key === 'Enter' && handleUpdate()}
                        className="text-xl font-medium text-[var(--foreground)] bg-transparent border-b border-blue-500 outline-none flex-1 mr-4"
                        autoFocus
                      />
                    ) : (
                      <h1
                        className="text-xl font-medium text-[var(--foreground)] cursor-pointer hover:bg-[var(--hover-bg)] hover:bg-opacity-50 p-1 rounded flex-1 mr-4"
                        onClick={() => setIsEditingTitle(true)}
                      >
                        {item?.name}
                      </h1>
                    )}
                    <button
                      onClick={onModalClose}
                      className="text-gray-400 hover:text-[var(--foreground)] p-1 rounded hover:bg-gray-600 hover:bg-opacity-50"
                    >
                      <X size={18} />
                    </button>
                  </div>
                  <div className="text-sm text-gray-400 mb-4">
                    in list <span className="underline cursor-pointer">Backlog</span>
                  </div>

                  {/* Action Buttons */}
                  <div className="flex flex-wrap gap-2">
                    <button className="flex items-center gap-2 px-3 py-2 border bg-opacity-50 hover:bg-opacity-70 text-[var(--foreground)] text-sm rounded">
                      <Calendar size={14} />
                      Dates
                    </button>
                    <button className="flex items-center gap-2 px-3 py-2 border bg-opacity-50 hover:bg-opacity-70 text-[var(--foreground)] text-sm rounded">
                      <List size={14} />
                      Checklist
                    </button>
                    {/* <button className="flex items-center gap-2 px-3 py-2 bg-gray-600 bg-opacity-50 hover:bg-opacity-70 text-[var(--foreground)] text-sm rounded">
                      <Users size={14} />
                      Members
                    </button> */}
                  </div>
                </div>
                <div className="pl-6 flex flex-wrap gap-2">

                  {/* Member section */}
                  <AssignMembers assignedMembers={item?.assignedMembers} taskId={Number(taskId)} />

                  {/* Labels Section */}
                  <div className="pb-4">
                    <h3 className="text-sm font-medium text-[var(--foreground)] mb-2">Labels</h3>
                    <div className="flex items-center gap-1 mb-1">
                      <span className="px-2 py-2 bg-green-600 text-[var(--foreground)] text-xs font-medium rounded">BE</span>
                      <button className="w-8 h-8 bg-gray-600 bg-opacity-50 hover:bg-opacity-70 text-gray-300 text-lg rounded flex items-center justify-center leading-none">+</button>
                    </div>
                  </div>
                </div>


                {/* Description Section */}
                <div className="px-6 pb-4">
                  <div className="flex items-center justify-between mb-3">
                    <h3 className="text-lg font-medium text-[var(--foreground)] flex items-center gap-2">
                      <List size={18} />
                      Description
                    </h3>
                    <button
                      onClick={() => setIsEditingDescription(!isEditingDescription)}
                      className="text-gray-400 hover:text-[var(--foreground)] p-1.5 hover:bg-[var(--hover-bg)] hover:bg-opacity-50 rounded"
                    >
                      <Edit3 size={16} />
                    </button>
                  </div>

                  {isEditingDescription ? (
                    <div>
                      <textarea
                        value={item.description || ""}
                        onChange={(e) => setItem({ ...item, description: e.target.value })}
                        className="w-full h-32 p-3 bg-[var(--background)] text-[var(--foreground)] rounded text-sm resize-none outline-none border border-gray-600 focus:border-blue-500"
                        placeholder="Add a more detailed description..."
                      />
                      <div className="flex gap-2 mt-3">
                        <button
                          onClick={() => {
                            setIsEditingDescription(false);
                            handleUpdate();
                          }}
                          className="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white text-sm rounded"
                        >
                          Save
                        </button>
                        <button
                          onClick={() => {
                            setIsEditingDescription(false);
                          }}
                          className="px-3 py-1.5 bg-transparent hover:bg-[var(--hover-bg)] hover:bg-opacity-50 text-[var(--foreground)] text-sm rounded"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div
                      onClick={() => setIsEditingDescription(true)}
                      className="bg-[var(--background)] p-3 rounded text-[var(--foreground)] text-sm leading-relaxed cursor-pointer"
                    >
                      {item?.description || "Add a more detailed description..."}
                    </div>
                  )}
                </div>

                {/* Activity Section */}
                <div className="px-6 pb-6">
                  {/* <h3 className="text-lg font-medium text-[var(--foreground)] mb-4 flex items-center gap-2">
                  <MessageSquareText />
                  Comments
                </h3> */}

                  {/* Comment Input */}
                  {/* <div className="flex gap-3 mb-4">
                  <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-[var(--foreground)] text-sm font-medium flex-shrink-0">
                    A
                  </div>
                  <div className="flex-1">
                    <textarea
                      value={comment}
                      onChange={(e) => setComment(e.target.value)}
                      placeholder="Write a comment..."
                      className="w-full p-3 bg-[#22272b] text-[var(--foreground)] rounded text-sm resize-none outline-none border border-gray-600 focus:border-blue-500"
                      rows={3}
                    />
                    {comment && (
                      <div className="flex gap-2 mt-2">
                        <button 
                          onClick={() => setComment('')}
                          className="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-[var(--foreground)] text-sm rounded"
                        >
                          Save
                        </button>
                      </div>
                    )}
                  </div>
                </div> */}

                  {/* Activity Item */}
                  {/* <div className="flex gap-3 text-sm">
                  <div className="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center text-[var(--foreground)] text-xs font-medium flex-shrink-0">
                    A
                  </div>
                  <div className="flex-1">
                    <div className="text-gray-300">
                      <span className="font-medium text-[var(--foreground)]">ABC</span> copied this card from{' '}
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
              <div className="w-48 bg-[var(--background)] border-l border-gray-600 border-opacity-50">
                <div className="p-4">
                  {/* Add to Card */}
                  <div className="mb-6">
                    <h4 className="text-xs font-medium text-gray-400 mb-3 uppercase tracking-wide">
                      ADD TO CARD
                    </h4>
                    <div className="space-y-1">
                      <button className="w-full text-left px-2 py-2 text-sm text-[var(--foreground)] hover:bg-[var(--hover-bg)] hover:bg-opacity-50 rounded flex items-center gap-2">
                        <Users size={14} />
                        Members
                      </button>
                      <button className="w-full text-left px-2 py-2 text-sm text-[var(--foreground)] hover:bg-[var(--hover-bg)] hover:bg-opacity-50 rounded flex items-center gap-2">
                        <div className="w-3.5 h-3.5 bg-gray-500 rounded"></div>
                        Labels
                      </button>
                      <button className="w-full text-left px-2 py-2 text-sm text-[var(--foreground)] hover:bg-[var(--hover-bg)] hover:bg-opacity-50 rounded flex items-center gap-2">
                        <List size={14} />
                        Checklist
                      </button>
                      <button className="w-full text-left px-2 py-2 text-sm text-[var(--foreground)] hover:bg-[var(--hover-bg)] hover:bg-opacity-50 rounded flex items-center gap-2">
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
                      <button
                        className="w-full text-left px-2 py-2 text-sm text-[var(--foreground)] hover:bg-[var(--hover-bg)] hover:bg-opacity-50 rounded flex items-center gap-2"
                        onClick={() => {
                          archiveTask(item.id);
                          onModalClose();
                        }}
                      >
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