import type { ApiResponse, UserProfile } from "@/types";
import axiosClients from "./axiosClient";

const profileUrl = "/user/profile";

export const userProfileService = {
    getUserProfile(): Promise<ApiResponse<UserProfile>> {
        return axiosClients.get(`${profileUrl}`);
    },

    createUserProfile(data: UserProfile): Promise<ApiResponse<UserProfile>> {
        return axiosClients.post(`${profileUrl}`, data);
    },

    updateUserProfile(data: Partial<UserProfile>): Promise<ApiResponse<UserProfile>> {
        return axiosClients.put(`${profileUrl}`, data);
    }
}