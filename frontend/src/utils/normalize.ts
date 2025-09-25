import type { Column, ColumnsState, Item, TasksState } from "@/types";

export const normalizeColumns = (columns: Column[]): ColumnsState => {
    return {
        byId: columns.reduce((acc, col) => {
            acc[col.id] = col;
            return acc;
        }, {} as Record<number, Column>),
        allIds: columns.map((col) => col.id),
    };
};

export const normalizeTasks = (items: Item[]): TasksState => {
    return {
        byId: items.reduce((acc, item) => {
            acc[item.id] = item;
            return acc;
        }, {} as Record<number, Item>),
        allIds: items.map((item) => item.id),
    };
}
