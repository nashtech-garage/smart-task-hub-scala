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
    position: number
}

export interface TasksState {
    byId: Record<number, Item>;
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
