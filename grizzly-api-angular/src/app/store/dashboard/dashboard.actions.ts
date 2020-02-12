import { Action } from '@ngrx/store';
import { Analytic } from 'src/app/shared/models/Analytic';

export const LOAD_ALL_ANALYTICS = '[Analytic] LOAD_ALL_ANALYTICS';
export const LOAD_ALL_ANALYTICS_SUCCESS = '[Analytic] LOAD_ALL_ANALYTICS_SUCCESS';



/** Load all Analytics */
export class LoadAllAnalytics implements Action {
    readonly type: string = LOAD_ALL_ANALYTICS;
    constructor(public payload: any = {}) { }
}

export class LoadAllAnalyticsSuccess implements Action {
    readonly type: string = LOAD_ALL_ANALYTICS_SUCCESS;
    constructor(public payload: Analytic) { }
}

export type DashboardActions =
    LoadAllAnalytics |
    LoadAllAnalyticsSuccess;
