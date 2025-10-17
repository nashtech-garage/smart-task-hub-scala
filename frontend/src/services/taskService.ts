import type { ApiResponse, Item, ItemDetail, Task, TaskSearchResponse, UpdateItemRequest } from "@/types";
import axiosClients from "./axiosClient";
import qs from "qs";

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

    getArchivedTasks(boardId: number): Promise<ApiResponse<Task[]>> {
        return axiosClients.get(`${projectUrl}/${boardId}/${columnUrl}/${taskUrl}/archived`);
    },

    restoreTask(taskId: number): Promise<ApiResponse<null>> {
        return axiosClients.patch(`/${taskUrl}/${taskId}/restore`);
    },

    deleteTask(taskId: number): Promise<ApiResponse<null>> {
        return axiosClients.delete(`/${taskUrl}/${taskId}`);
    },

    assignMember(projectId: number, taskId: number, memberId: number): Promise<ApiResponse<null>> {
        return axiosClients.post(`/${projectUrl}/${projectId}/${taskUrl}/${taskId}/members`, {
            userId: memberId
        });
    },

    removeMember(projectId: number, taskId: number, memberId: number): Promise<ApiResponse<null>> {
        return axiosClients.delete(`/${projectUrl}/${projectId}/${taskUrl}/${taskId}/members/${memberId}`);
    },

    searchTasks(keyword: string, projectIds?: number[], page: number = 1, size: number = 10): Promise<ApiResponse<TaskSearchResponse[]>> {
        return axiosClients.get("/tasks", {
            params: {
                page,
                size,
                keyword,
                projectIds,
            },
            paramsSerializer: (params) =>
                qs.stringify(params, { arrayFormat: "repeat" }),
        });
    },

    updateTaskPosition(taskId: number, columnId: number, position: number): Promise<ApiResponse<null>> {
        return axiosClients.patch(`/${taskUrl}/${taskId}/position`, {
            columnId,
            position
        })
    },

    getTaskByColumnId(projectId: number, columnId: number, pageParams: { page: number, limit: number }): Promise<ApiResponse<Task[]>> {
        return axiosClients.get(`${projectUrl}/${projectId}/${columnUrl}/${columnId}/${taskUrl}`, {
            params: pageParams
        })
    }

}

export default taskService;