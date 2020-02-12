import { Store } from '@ngrx/store';
import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot } from '@angular/router';
import * as dashboardActions from '../../store/dashboard/dashboard.actions';
import { DashboardState } from 'src/app/store/dashboard/dashboard.state';

@Injectable()
export class DashboardResolver implements Resolve<any> {
    constructor(private dashboardStore: Store<DashboardState>) { }

    resolve(route: ActivatedRouteSnapshot): any {
        return this.dashboardStore.dispatch(new dashboardActions.LoadAllAnalytics());
    }
}
