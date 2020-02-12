import { Action } from '@ngrx/store';

export const EFFECT_ERROR = '[EFFECT] EFFECT_ERROR';

export class EffectError implements Action {
    readonly type: string = EFFECT_ERROR;
    constructor(public payload: any) {}
}

export type GlobalActions = EffectError;
