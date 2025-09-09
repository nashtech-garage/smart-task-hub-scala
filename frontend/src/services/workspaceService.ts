
import type { ApiResponse } from '@/types';
import type { WorkSpace } from '@/types/workspace';
import axiosClients from './axiosClient';
import type { Board } from '@/types/project';

const baseUrl = '/workspaces';
const projectUrl = '/projects';

// Create a new workspace
const createWorkspace = async (
    name: string,
    description: string
): Promise<ApiResponse<{ message: string }>> => {
    return axiosClients.post(`${baseUrl}`, {
        name,
        description
    });
}

// Update an existing workspace
const updateWorkspace = async (
    workspaceId: number,
    name: string,
    description: string | null
): Promise<ApiResponse<{ message: string }>> => {
    return axiosClients.put(`${baseUrl}/${workspaceId}`, {
        name,
        description
    });
}

const getWorkspaces = async (): Promise<ApiResponse<WorkSpace[]>> => {
    return axiosClients.get(`${baseUrl}`);
};

const getWorkspaceById = async (id: number): Promise<ApiResponse<WorkSpace>> => {
    return axiosClients.get(`${baseUrl}/${id}`);
};

const deleteWorkspace = async (id: number): Promise<ApiResponse<String>> => {
    return axiosClients.delete(`${baseUrl}/${id}`);
};

const getBoards = async ( id: number ): Promise<ApiResponse<Board[]>> => {
    return axiosClients.get(`${baseUrl}/${id}${projectUrl}`)
};

const createBoard = async (
    name: string,
    workspaceId: number,
    visibility?: string
): Promise<ApiResponse<null>> => {
    return axiosClients.post(`${baseUrl}/${workspaceId}${projectUrl}`, {
        name,
        workspaceId,
        visibility
    });
};

const completedBoard = async (id: number): Promise<ApiResponse<null>> => {
    return axiosClients.patch(`${projectUrl}/${id}/complete`);
};

const reopenBoard = async (id: number): Promise<ApiResponse<null>> => {
    return axiosClients.patch(`${projectUrl}/${id}/reopen`);
};

const deleteBoard = async (id: number): Promise<ApiResponse<null>> => {
    return axiosClients.patch(`${projectUrl}/${id}/delete`);
};

const getClosedBoards = async (): Promise<ApiResponse<WorkSpace[]>> => {
    return axiosClients.get(`${projectUrl}/completed`);
};

export { getWorkspaces, createWorkspace, updateWorkspace, getWorkspaceById, deleteWorkspace, getBoards, createBoard, completedBoard, reopenBoard, deleteBoard, getClosedBoards };
