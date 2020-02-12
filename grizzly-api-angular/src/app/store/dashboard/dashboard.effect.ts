import { Injectable } from '@angular/core';
import { Effect, Actions, ofType } from '@ngrx/effects';
import * as dashboardActions from './dashboard.actions';
import * as globalActions from '../global.actions';
import { switchMap, map, catchError } from 'rxjs/operators';

// PROJECT SERVICE FOR HTTP CALLS
import { DashboardService } from '../../layout/dashboard/dashboard.service';
import { of } from 'rxjs';



@Injectable()
export class DashboardEffects {

  constructor(private actions: Actions, private dashboardService: DashboardService
  ) { }

  @Effect()
  loadProjects = this.actions.pipe(
    ofType(dashboardActions.LOAD_ALL_ANALYTICS),
    switchMap(() => {
      return this.dashboardService.getAnalytics().pipe(
        map(analytics => new dashboardActions.LoadAllAnalyticsSuccess(analytics)),
        catchError(() => of(new globalActions.EffectError({})))
      );
    }),
  );

}
