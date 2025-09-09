import type { ApiResponse, Board, Column, UrlPreviewData } from '@/types';
import axiosClients from './axiosClient';

const previewUrl = '/url-preview';
const projectUrl = '/projects';

const fetchUrlPreview = async (url: string): Promise<UrlPreviewData> => {
    return axiosClients.get(`${previewUrl}`, {
        params: {
            url,
        },
    });
};

const fetchBoardDetail = async (id: number): Promise<ApiResponse<Board>> => {
    return axiosClients.get(`${projectUrl}/${id}`);
};

const fetchBoardColumns = async (id: number): Promise<ApiResponse<Column[]>> => {
    return axiosClients.get(`${projectUrl}/${id}/columns`);
};

const fetchArchivedColumns = async (id: number): Promise<ApiResponse<Column[]>> => {
    return axiosClients.get(`${projectUrl}/${id}/columns/archived`);
};

const createNewColumn = async (id: number, title: string, position: number): Promise<ApiResponse<null>> => {
    return axiosClients.post(`${projectUrl}/${id}/columns`, {
        name: title,
        position
    });
};

const updateColumn = async (id: number, boardId: number, name: string): Promise<ApiResponse<null>> => {
    return axiosClients.patch(`${projectUrl}/${id}/columns/${boardId}`, {
        name
    });
};

const updateColumnPosititon = async (boardId: number, columnId: number, position: number): Promise<ApiResponse<null>> => {
    return axiosClients.patch(`${projectUrl}/${boardId}/columns/${columnId}/position`, {
        position
    });
};

const archiveColumn = async (columnId: number): Promise<ApiResponse<null>> => {
    return axiosClients.patch(`/columns/${columnId}/archive`);
};

const restoreColumn = async (columnId: number): Promise<ApiResponse<null>> => {
    return axiosClients.patch(`/columns/${columnId}/restore`);
};

const deleteColumn = async (columnId: number): Promise<ApiResponse<null>> => {
    return axiosClients.delete(`/columns/${columnId}`);
};

export { 
    fetchUrlPreview, fetchBoardDetail, createNewColumn, 
    updateColumn, archiveColumn, restoreColumn, 
    deleteColumn, updateColumnPosititon, fetchBoardColumns, 
    fetchArchivedColumns 
};
