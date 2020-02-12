import { Store } from '@ngrx/store';
import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, ActivatedRoute } from '@angular/router';
import { ProjectsState } from '../../store/project/project.state';
import * as projectActions from '../../store/project/project.actions';
import { ProjectService } from 'src/app/project/project.service';


@Injectable()
export class ProjectsResolver implements Resolve<any> {
  constructor(private activatedRoute: ActivatedRoute, private store: Store<ProjectsState>, private projectService: ProjectService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): any {
      return this.store.dispatch(new projectActions.LoadAllProjects({}));
  }
}
