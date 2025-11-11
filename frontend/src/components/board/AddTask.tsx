import { Plus, X } from "lucide-react";
import UrlPreview from "./UrlPreview";
import { useSelector } from "react-redux";
import { selectColumnToggleStates } from "@/store/selectors/modalSelector";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { detectUrl } from "@/utils/UrlPreviewUtils";
import { resetcolumnToggleStates, showAddTaskForm } from "@/store/slices/columnToggleStatesSlice";
import { useAppDispatch } from "@/store";
import taskService from "@/services/taskService";
import { notify } from "@/services/toastService";
import { selectMaxTaskPositionByColumn } from "@/store/selectors/tasksSelectors";

interface AddTaskProps {
  columnId: number;
}

const AddTask: React.FC<AddTaskProps> = ({ columnId }) => {
  const [taskTitle, setTaskTitle] = useState("");
  const [detectedUrl, setDetectedUrl] = useState<string | null>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const formRef = useRef<HTMLDivElement>(null);

  const { isAddingTask, columnId: currentToggleColumn } = useSelector(selectColumnToggleStates);
  const maxPosition = useSelector(selectMaxTaskPositionByColumn(columnId));
  const dispatch = useAppDispatch();

  // Detect URL
  const memorizedDetectedUrl = useMemo(() => (taskTitle ? detectUrl(taskTitle) : null), [taskTitle]);
  useEffect(() => setDetectedUrl(memorizedDetectedUrl), [memorizedDetectedUrl]);

  const handleSubmitAddTask = useCallback(async () => {
    try {
      if (!taskTitle.trim()) return;

      const newPosition = maxPosition + 1000;
      const result = await taskService.createTask(columnId, taskTitle.trim(), newPosition);
      notify.success(result.message);
      setTaskTitle("");
      dispatch(resetcolumnToggleStates());
    } catch (error: any) {
      notify.error(error.response?.data?.message || "Failed to create card");
    }
  }, [columnId, maxPosition, taskTitle, dispatch]);

  const handleRemoveUrlPreview = useCallback(() => {
    if (detectedUrl) {
      setTaskTitle(taskTitle.replace(detectedUrl, "").trim());
      setDetectedUrl(null);
    }
  }, [detectedUrl, taskTitle]);

  const handleInputAreaClick = useCallback((e: React.MouseEvent) => e.stopPropagation(), []);

  const cancelAddingTask = useCallback(() => {
    setTaskTitle("");
    dispatch(resetcolumnToggleStates());
  }, [dispatch]);

  const handleAddTaskClick = useCallback(() => {
    dispatch(showAddTaskForm(columnId));
  }, [dispatch, columnId]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        handleSubmitAddTask();
      } else if (e.key === "Escape") {
        cancelAddingTask();
      }
    },
    [handleSubmitAddTask, cancelAddingTask]
  );

  // Cancel adding task when clicking outside
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (formRef.current && !formRef.current.contains(e.target as Node)) {
        dispatch(resetcolumnToggleStates());
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [dispatch]);

  useEffect(() => {
    if (isAddingTask) {
      inputRef.current?.focus();
      inputRef.current?.select();
    }
  }, [isAddingTask, inputRef, currentToggleColumn]);

  return (
    <div className="flex-shrink-0 mt-3 pr-1">
      {isAddingTask && columnId === currentToggleColumn ? (
        <div className="space-y-2" onClick={handleInputAreaClick}>
          {detectedUrl && (
            <UrlPreview url={detectedUrl} onRemove={handleRemoveUrlPreview} showRemoveButton />
          )}

          <textarea
            ref={inputRef}
            value={taskTitle}
            onChange={(e) => setTaskTitle(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Enter a title or paste a link"
            className="w-full p-2 text-sm bg-[var(--background)] text-[var(--foreground)] border border-[#394B59] rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            rows={3}
          />
          <div className="flex items-center gap-2">
            <button
              onClick={handleSubmitAddTask}
              disabled={!taskTitle.trim()}
              className="px-3 py-1.5 text-sm bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white rounded transition-colors"
            >
              Add Task
            </button>
            <button
              onClick={cancelAddingTask}
              className="p-1.5 text-gray-400 hover:text-gray-300 hover:bg-[#22272B] rounded transition-colors"
            >
              <X size={16} />
            </button>
          </div>
        </div>
      ) : (
        <button
          onClick={handleAddTaskClick}
          className="w-full p-2 text-[var(--task-title)] hover:text-[var(--task-title)] hover:bg-[var(--task-bg)] rounded-lg transition-colors flex items-center gap-2"
        >
          <Plus size={16} />
          Add a task
        </button>
      )}
    </div>
  );
};

export default AddTask;