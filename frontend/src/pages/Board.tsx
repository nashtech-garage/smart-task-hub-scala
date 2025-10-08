import { useSelector } from "react-redux";
import WorkspaceBoard from "../components/board/BoardContent";
import { selectTaskModalState } from "@/store/selectors/modalSelector";
import TaskDetailModal from "@/components/board/TaskDetailModal";

const Board = () => {
    const { isTaskModalShow } = useSelector(selectTaskModalState);
    return (
        <>
            <WorkspaceBoard />
            {isTaskModalShow && <TaskDetailModal />}
        </>
    );
}

export default Board;