import * as dashboardActions from './dashboard.actions';
import * as globalActions from '../global.actions';
import { Analytic } from 'src/app/shared/models/Analytic';
import { DashboardState } from './dashboard.state';

const initialState: DashboardState = {
    analytics: new Analytic(),
    loading: true
};

export function dashboardReducer(state = initialState, action: dashboardActions.DashboardActions): DashboardState {
    switch (action.type) {

        case dashboardActions.LOAD_ALL_ANALYTICS_SUCCESS:
            const newState = Object.assign({}, state);
            newState.analytics = (action.payload as Analytic);
            newState.loading = false;
            return newState;

        case globalActions.EFFECT_ERROR:
            return Object.assign({}, state);

        default:
            return state;
    }
}

