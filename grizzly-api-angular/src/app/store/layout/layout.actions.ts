import { Action } from '@ngrx/store';

export const TOGGLE_RG = '[Layout] TOGGLE_RG';
export const CLOSE_RG = '[Layout] CLOSE_RG';

export class ToggleRg implements Action {
    readonly type: string = TOGGLE_RG;
    constructor(public containerId: string, public rgIndex: number) { }
}

export type LayoutActions = ToggleRg ;
