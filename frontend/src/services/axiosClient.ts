import axios, { type AxiosInstance, type AxiosResponse } from 'axios';

const axiosClients: AxiosInstance = axios.create({
    baseURL:
        `${import.meta.env.VITE_TRELLO_LIKE_API_URL}/api` ||
        'http://localhost:9000/api',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

axiosClients.interceptors.request.use(
    function (config) {
        // Add any custom logic before the request is sent
        return config;
    },
    function (error) {
        return Promise.reject(error);
    }
);

axiosClients.interceptors.response.use(
    function (response: AxiosResponse) {
        return response.data;
    },
    function (error) {
        return Promise.reject(error);
    }
);
export default axiosClients;
