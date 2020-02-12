import { Injectable } from '@angular/core';
import { Effect, Actions, ofType } from '@ngrx/effects';
import * as dbsourceActions from './dbsource.actions';
import * as globalActions from '../global.actions';
import { switchMap, map, catchError } from 'rxjs/operators';

// DBSource SERVICE FOR HTTP CALLS
import { DBSourceService } from 'src/app/dbsource/dbsource.service';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';

@Injectable()
export class DBSourceEffects {

  constructor(private router: Router,
              private actions: Actions,
              private dbsourceService: DBSourceService,
              private toaster: ToastrService) { }

  @Effect()
  loadDBSources = this.actions.pipe(
    ofType(dbsourceActions.LOAD_ALL_DBSOURCES),
    switchMap(() => {
      return this.dbsourceService.getAll().pipe(
        map(dbSources => new dbsourceActions.LoadAllDBSourcesSuccess(dbSources)),
        catchError(() => of(new globalActions.EffectError({})))
      );
    })
  );

  @Effect()
  createNewDBSource = this.actions.pipe(
    ofType(dbsourceActions.ADD_DBSOURCE),
    switchMap((action: dbsourceActions.AddDBSource) => {
      return this.dbsourceService.saveDBSource(action.payload).pipe(
        map(dbsource => {
          this.toaster.success('New DataSource Added');
          // this.router.navigateByUrl('/app/dbsource');
          return new dbsourceActions.AddDBSourceSuccess(dbsource);
        }),
        catchError(() => of(new globalActions.EffectError({})))
      );
    }
    )
  );

  @Effect()
  updateDBSource = this.actions.pipe(
    ofType(dbsourceActions.UPDATE_DBSOURCE),
    switchMap((action: dbsourceActions.UpdateDBSource) => {
      return this.dbsourceService.saveDBSource(action.payload).pipe(
        map(dbsource => {
          this.toaster.success('DataSource Updated');
          return new dbsourceActions.UpdateDBSourceSuccess(dbsource);
        }),
        catchError(() => of(new globalActions.EffectError({})))
      );
    })
  );

  @Effect()
  deleteDBSource = this.actions.pipe(
    ofType(dbsourceActions.DELETE_DBSOURCE),
    map((action: dbsourceActions.DeleteDBSource) => {
      this.dbsourceService.deleteDBSource(action.payload).subscribe(() => {
        this.toaster.success('Datasource Deleted');
      });
      return new dbsourceActions.DeleteDBSourceSuccess(action.payload);
    }),
    catchError(() => of(new globalActions.EffectError({})))
  );

  @Effect()
  addNewCollection = this.actions.pipe(
    ofType(dbsourceActions.ADD_NEW_COLLECTION),
    switchMap((action: dbsourceActions.AddNewCollection) => {
      return this.dbsourceService.saveNewCollection(action.payload.containerId, action.payload.collectionName).pipe(
        map((res) => {
          if (res === true) {
            this.toaster.success('New Collection Added');
            return new dbsourceActions.AddNewCollectionSuccess({ dbsourceId: action.payload.dbsourceId, databaseName: action.payload.databaseName, containerId: action.payload.containerId, collectionName: action.payload.collectionName });
          } else {
            this.toaster.error('New Collection can\'t be added');
          }
        }),
        catchError(() => of(new globalActions.EffectError({})))
      );
    })
  );

  @Effect()
  dropCollection = this.actions.pipe(
    ofType(dbsourceActions.DROP_COLLECTION),
    switchMap((action: dbsourceActions.DropCollection) => {
      return this.dbsourceService.dropCollection(action.payload.dbsourceId, action.payload.databaseName, action.payload.collectionName).pipe(
        map(res => {
          this.toaster.success('Collection ' + action.payload.collectionName + ' dropped');
          return new dbsourceActions.DropCollectionSuccess({ dbsourceId: action.payload.dbsourceId, databaseName: action.payload.databaseName, containerId: null, collectionName: action.payload.collectionName });
        }),
        catchError(() => of(new globalActions.EffectError({})))
      );
    })
  );
}
