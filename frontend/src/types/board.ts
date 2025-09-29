export interface UrlPreviewData {
    url: string;
    title?: string;
    description?: string;
    image?: string;
    siteName?: string;
    favicon?: string;
}

export interface Board {
    id: number;
    name: string;
    status: "active" | "completed" | "deleted" | undefined
}

export interface Item {
    id: number;
    name: string;
    urlPreview?: UrlPreviewData;
    position: number
}

export interface Task {
    id: number;
    name: string;
    urlPreview?: UrlPreviewData;
    position: number;
    detail?: TaskDetail;
}

export interface TasksState {
    byId: Record<number, Task>;
    allIds: number[];
}

export interface Column {
    id: number;
    position: number;
    name: string;
    taskIds: number[];
}

export interface ColumnsState {
    byId: Record<number, Column>;
    allIds: number[];
}

export interface UpdateItemRequest {
    name: string;
    description: string;
}

export interface ItemDetail extends Item {
    description?: string;
}

export interface TaskDetail {
    name: string;
    description?: string;
    startDate?: string;
    endDate?: string;
    priority?: "LOW" | "MEDIUM" | "HIGH";
    status: "active" | "archived" | "deleted";
    columnId: number;
    position: number;
    isCompleted: boolean;
    createdAt: string;
    updatedAt: string;
}
