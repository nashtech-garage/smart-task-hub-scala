import { Ellipsis } from "lucide-react";
import { useCallback, useEffect, useRef } from "react";
import ColumnOptionsMenu from "./ColumnOptionsMenu";
import { archiveColumnThunk } from "@/store/thunks/columnsThunks";
import type { Column } from "@/types";
import { useAppDispatch } from "@/store";
import { updateColumn } from "@/services/boardService";
import { notify } from "@/services/toastService";
import { useSelector } from "react-redux";
import { selectColumnToggleStates } from "@/store/selectors/modalSelector";
import {
    resetcolumnToggleStates,
    showInputTitleForm,
    showOptionMenu,
} from "@/store/slices/columnToggleStatesSlice";
import { useParams } from "react-router-dom";

interface ColumnHeaderProps {
    columnRef: React.RefObject<HTMLDivElement | null>;
    attributes: any;
    listeners: any;
    column: Column;
}

const ColumnHeader: React.FC<ColumnHeaderProps> = ({
    columnRef,
    attributes,
    listeners,
    column,
}) => {
    console.log("header render", column.id);
    const titleInputRef = useRef<HTMLInputElement>(null);
    const dispatch = useAppDispatch();
    const { boardId } = useParams();

    const { isInputtingTitle, isShowingOptionMenu, columnId: currentToggleColumn } =
        useSelector(selectColumnToggleStates);

    const updateColumnTitle = useCallback(
        async (columnId: number, newTitle: string) => {
            try {
                const result = await updateColumn(Number(boardId), columnId, newTitle);
                notify.success(result.message);
            } catch (error: any) {
                notify.error(error.response?.data?.message || "Failed to update column");
            }
        },
        [boardId]
    );

    const handleTitleSubmit = useCallback(() => {
        const newValue = titleInputRef.current?.value.trim();
        if (!newValue) {
            if (titleInputRef.current) titleInputRef.current.value = column.name;
        } else if (newValue !== column.name) {
            updateColumnTitle(column.id, newValue);
        }
        dispatch(resetcolumnToggleStates());
    }, [column.id, column.name, updateColumnTitle, dispatch]);

    const handleTitleKeyDown = useCallback(
        (e: React.KeyboardEvent) => {
            if (e.key === "Enter") {
                e.preventDefault();
                handleTitleSubmit();
            } else if (e.key === "Escape") {
                if (titleInputRef.current) titleInputRef.current.value = column.name;
                dispatch(resetcolumnToggleStates());
            }
        },
        [handleTitleSubmit, column.name, dispatch]
    );

    useEffect(() => {
        if (isInputtingTitle) {
            titleInputRef.current?.focus();
            titleInputRef.current?.select();
        }
    }, [isInputtingTitle]);

    const handleTitleInputClick = useCallback((e: React.MouseEvent) => {
        e.stopPropagation();
    }, []);

    const handleTitleClick = useCallback(
        (e: React.MouseEvent) => {
            e.stopPropagation();
            dispatch(showInputTitleForm(column.id));
        },
        [dispatch]
    );

    const handleOptionsMenuToggle = useCallback(
        (e: React.MouseEvent) => {
            e.stopPropagation();
            dispatch(showOptionMenu(column.id));
        },
        [dispatch]
    );

    const handleArchiveColumn = useCallback(async () => {
        dispatch(archiveColumnThunk(column) as any);
    }, [dispatch]);

    const handleArchiveAllItems = useCallback(() => {
        // Implement archive all items logic here
    }, []);

    return (
        <div
            ref={columnRef}
            {...attributes}
            {...listeners}
            className="flex-shrink-0 flex items-center justify-between gap-4 cursor-grab p-2 pb-3"
        >
            <div className="flex items-center gap-2 flex-1">
                {isInputtingTitle && currentToggleColumn === column.id ? (
                    <input
                        ref={titleInputRef}
                        defaultValue={column.name}
                        onKeyDown={handleTitleKeyDown}
                        onBlur={handleTitleSubmit}
                        onClick={handleTitleInputClick}
                        className="font-semibold text-[var(--column-header-text)] bg-[var(--column-header-bg)] border border-[#394B59] rounded px-2 py-1 -mx-2 -my-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent flex-1"
                    />
                ) : (
                    <h3
                        className="font-semibold text-[var(--column-header-text)] cursor-pointer border border-transparent hover:bg-[var(--column-header-bg)] rounded px-2 py-1 -mx-2 -my-1 transition-colors flex-1"
                        onClick={handleTitleClick}
                    >
                        {column.name}
                    </h3>
                )}
            </div>

            <div className="relative">
                <button
                    onClick={handleOptionsMenuToggle}
                    className="text-gray-400 hover:text-[var(--foreground)] hover:bg-[var(--column-header-bg)] rounded p-1 transition-colors flex-shrink-0"
                >
                    <Ellipsis size={16} />
                </button>

                <ColumnOptionsMenu
                    isOpen={isShowingOptionMenu && currentToggleColumn === column.id}
                    onClose={() => dispatch(resetcolumnToggleStates())}
                    onArchiveColumn={handleArchiveColumn}
                    onArchiveAllItems={handleArchiveAllItems}
                />
            </div>
        </div>
    );
};

export default ColumnHeader;
