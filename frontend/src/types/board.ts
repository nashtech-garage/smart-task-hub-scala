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
    content: string;
    urlPreview?: UrlPreviewData;
}

export interface Column {
    id: number;
    position: number;
    name: string;
    tasks: Item[];
}
