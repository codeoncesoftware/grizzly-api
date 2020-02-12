import { Injectable } from '@angular/core';
import { Effect, Actions, ofType } from '@ngrx/effects';
import * as projectActions from './project.actions';
import * as globalActions from '../global.actions';
import { switchMap, map, catchError } from 'rxjs/operators';

// PROJECT SERVICE FOR HTTP CALLS
import { ProjectService } from '../../project/project.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Action } from '@ngrx/store';
import { of } from 'rxjs';
import { AppTranslateService } from 'src/app/shared/services/app-translate-service';

export class EffectError implements Action {
  readonly type = '[Error] Effect Error';
}

@Injectable()
export class ProjectEffects {

  constructor(private router: Router,
    // tslint:disable: align
    private actions: Actions,
    private projectService: ProjectService,
    private toaster: ToastrService,
    private translateService: AppTranslateService
    // tslint:enable: align
  ) { }

  @Effect()
  loadProjects = this.actions.pipe(
    ofType(projectActions.LOAD_ALL_PROJECTS),
    switchMap(() => {
      return this.projectService.getAllProjects().pipe(
        map(projects => new projectActions.LoadAllProjectsSuccess(projects)),
        catchError(() => of(new globalActions.EffectError({})))
      );
    }),
  );


  @Effect()
  createNewProject = this.actions.pipe(
    ofType(projectActions.ADD_PROJECT),
    switchMap((action: projectActions.AddProject) => {
      return this.projectService.createProject(action.payload).pipe(
        map(proj => {
          this.toaster.success('Project Added');
          this.router.navigateByUrl('/app/project/' + proj.id);
          return new projectActions.AddProjectSuccess(proj);
        }),
        catchError(() => of(new globalActions.EffectError({})))
      );
    }
    ));

  @Effect()
  deleteProject = this.actions.pipe(
    ofType(projectActions.DELETE_PROJECT),
    map((action: projectActions.DeleteProject) => {
      this.projectService.deleteProjectById(action.payload).subscribe(() => {
        this.toaster.success('Project Deleted');
      });
      return new projectActions.DeleteProjectSucess(action.payload);
    }),
    catchError(() => of(new globalActions.EffectError({})))
  );

  @Effect()
  updateProject = this.actions.pipe(
    ofType(projectActions.UPDATE_PROJECT),
    map((action: projectActions.UpdateProject) => {
      this.projectService.updateProject(action.payload).subscribe((a) => {
        action.payload.lastUpdate = new Date().toUTCString();
        this.toaster.success(this.translateService.getMessage('toastr.projectUpdated'));
      });
      return new projectActions.UpdateProjectSuccess(action.payload);

    }),
    catchError(() => of(new globalActions.EffectError({})))
  );

}
