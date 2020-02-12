import { Store } from '@ngrx/store';
import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot } from '@angular/router';
import * as dbsourceActions from '../../store/dbsource/dbsource.actions';
import { DBSourceService } from 'src/app/dbsource/dbsource.service';
import { DBSourcesState } from 'src/app/store/dbsource/dbsource.state';

@Injectable()
export class ActiveDBSourceResolver implements Resolve<any> {

    // Global State
    DBSourcesState: any;
    // To check if it is already set and ignore state refresh
    isSet = false;

    constructor(private dbsourceService: DBSourceService, private dbsourceStore: Store<DBSourcesState>, private store: Store<DBSourcesState>) { }

    resolve(route: ActivatedRouteSnapshot): any {
        this.isSet = false;
        this.dbsourceStore.subscribe(res => {
            this.DBSourcesState = res;
            if (!this.isSet && (this.DBSourcesState.dbsources.dbsources && !this.DBSourcesState.dbsources.active) || (this.DBSourcesState.dbsources.active && this.DBSourcesState.dbsources.active.id !== route.params.id) ) {
                if (this.DBSourcesState.dbsources.dbsources.length === 0 && !this.isSet) {
                    this.isSet = true;
                    return this.dbsourceService.getDBSourceById(route.params.id).subscribe(dbsource => {
                        return this.dbsourceStore.dispatch(new dbsourceActions.LoadActiveDbsource(dbsource));
                    });
                } else if (!this.isSet && (this.DBSourcesState.dbsources.dbsources.length > 0)) {
                    const activeDB = this.DBSourcesState.dbsources.dbsources.find(element => element.id === route.params.id);
                    this.isSet = true;
                    return this.dbsourceStore.dispatch(new dbsourceActions.LoadActiveDbsource(activeDB));
                }
            }
        });
    }
}
