import type { ApiResponse, Item, ItemDetail, UpdateItemRequest } from "@/types";
import axiosClients from "./axiosClient";

const projectUrl = 'projects';
const columnUrl = 'columns';
const taskUrl = 'tasks';

const taskService = {
    createTask(columnId: number, name: string, position: number): Promise<ApiResponse<null>> {
        return axiosClients.post(`/${columnUrl}/${columnId}/${taskUrl}`, {
            name,
            position
        });
    },

    getTaskDetail(taskId: number): Promise<ApiResponse<ItemDetail>> {
        return axiosClients.get(`/${taskUrl}/${taskId}`);
    },

    updateTask(taskId: number, data: UpdateItemRequest): Promise<ApiResponse<null>> {
        console.log('Updating task:', taskId, data);
        return axiosClients.patch(`/${taskUrl}/${taskId}`, data);
    },

    archiveTask(taskId: number): Promise<ApiResponse<null>> {
        return axiosClients.patch(`/${taskUrl}/${taskId}/archive`);
    },

    getArchivedTasks(boardId: number): Promise<ApiResponse<Item[]>> {
        return axiosClients.get(`${projectUrl}/${boardId}/${columnUrl}/${taskUrl}/archived`);
    },

    restoreTask(taskId: number): Promise<ApiResponse<null>> {
        return axiosClients.patch(`/${taskUrl}/${taskId}/restore`);
    },

    deleteTask(taskId: number): Promise<ApiResponse<null>> {
        return axiosClients.delete(`/${taskUrl}/${taskId}`);
    },

}

export default taskService;