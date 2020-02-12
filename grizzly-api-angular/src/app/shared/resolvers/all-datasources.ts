import { Injectable } from '@angular/core';
import { Resolve, ActivatedRoute, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Store } from '@ngrx/store';
import * as dbsource from './../../store/dbsource/dbsource.actions';
import { DBSourcesState } from 'src/app/store/dbsource/dbsource.state';
import { DBSourceService } from 'src/app/dbsource/dbsource.service';

@Injectable()
export class DBSourcesResolver implements Resolve<any> {
  constructor(private activatedRoute: ActivatedRoute, private store: Store<DBSourcesState>, private projectService: DBSourceService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot, ): any {
    return this.store.dispatch(new dbsource.LoadAllDBSources({}));
  }
}
