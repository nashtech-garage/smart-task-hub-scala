export interface Member {
    id: string;
    name: string;
    username: string;
    avatar?: string;
    lastActive: string;
    boardCount: number;
    isAdmin: boolean;
}

export interface Guest {
    id: string;
    name: string;
    username: string;
    avatar?: string;
    lastActive: string;
    boardCount: number;
}

export interface JoinRequest {
    id: string;
    name: string;
    username: string;
    avatar?: string;
    requestDate: string;
}