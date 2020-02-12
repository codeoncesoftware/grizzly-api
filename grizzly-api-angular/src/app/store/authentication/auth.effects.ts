import { Injectable } from '@angular/core';
import * as authActions from './auth.actions';
import * as globalActions from '../global.actions';
import { AuthService } from 'src/app/auth/auth.service';
import { Effect, Actions, ofType } from '@ngrx/effects';
import { switchMap, map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable()
export class AuthEffects {

  constructor(private actions: Actions,
              private authService: AuthService) { }

  @Effect()
  login = this.actions.pipe(
    ofType(authActions.LOGIN_USER),
    switchMap((action: authActions.LoginUser) => {
      return this.authService.getUser(action.payload).pipe(
        map(user => {
          localStorage.setItem('firstName', user.firstName);
          localStorage.setItem('lastName', user.lastName);
          localStorage.setItem('username', user.username);
          return new authActions.LoginUserSuccess(user);
        }),
        catchError(() => of(new globalActions.EffectError({})))
      );
    })
  );

}
