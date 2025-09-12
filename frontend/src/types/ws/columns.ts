export type ColumnInMsg = 
    | { type: 'moveColumn'; boardId: number; columnId: number; newPosition: number };

export type ColumnOutMsg = 
    | { type: 'columnMoved'; columnId: number, newPosition: number }