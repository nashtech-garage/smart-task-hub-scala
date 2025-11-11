import type { UserProfile } from "@/types";
import axiosClients from "./axiosClient";

const profileUrl = "/user/profile";

export const userProfileService = {
    getUserProfile(): Promise<UserProfile> {
        return axiosClients.get(`${profileUrl}`);
    },

    createUserProfile(data: UserProfile): Promise<UserProfile> {
        return axiosClients.post(`${profileUrl}`, data);
    },

    updateUserProfile(data: Partial<UserProfile>): Promise<UserProfile> {
        return axiosClients.put(`${profileUrl}`, data);
    }
}