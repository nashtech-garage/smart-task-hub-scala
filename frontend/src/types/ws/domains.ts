export type DomainInMsg = 
    | { type: 'ping' }
    | { type: 'join'; boardId: number; };

export type DomainOutMsg = 
    | { type: 'pong' }
    | { type: 'joined'; boardId: number }
    | { type: 'error'; message: string };