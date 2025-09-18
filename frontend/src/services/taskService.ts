import type { ApiResponse } from "@/types";
import axiosClients from "./axiosClient";

const columnUrl = '/columns';

const taskService = {
    createTask(columnId: number, name: string, position: number): Promise<ApiResponse<null>> {
        return axiosClients.post(`${columnUrl}/${columnId}/tasks`, {
            name,
            position
        });
    },

}

export default taskService;