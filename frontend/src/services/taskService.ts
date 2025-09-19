import type { ApiResponse } from "@/types";
import axiosClients from "./axiosClient";

const columnUrl = '/columns';
const taskUrl = '/tasks';

const taskService = {
    createTask(columnId: number, name: string, position: number): Promise<ApiResponse<null>> {
        return axiosClients.post(`${columnUrl}/${columnId}/${taskUrl}`, {
            name,
            position
        });
    },

    archiveTask(taskId: number): Promise<ApiResponse<null>> {
        return axiosClients.patch(`${taskUrl}/${taskId}/archive`);
    },

}

export default taskService;