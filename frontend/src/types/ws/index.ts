import type { ColumnInMsg, ColumnOutMsg } from "./columns";
import type { DomainInMsg, DomainOutMsg } from "./domains";

export type InMsg = DomainInMsg | ColumnInMsg;
export type OutMsg = DomainOutMsg | ColumnOutMsg;