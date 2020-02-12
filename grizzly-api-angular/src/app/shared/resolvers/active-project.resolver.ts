import { Store } from '@ngrx/store';
import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot } from '@angular/router';
import { ProjectsState } from '../../store/project/project.state';
import * as projectActions from '../../store/project/project.actions';
import * as containerActions from '../../store/container/container.actions';
import { ProjectService } from 'src/app/project/project.service';
import { ContainerState } from 'src/app/store/container/container.state';
import { ContainerService } from 'src/app/container/container.service';

@Injectable()
export class ActiveProjectResolver implements Resolve<any> {
    constructor(private projectService: ProjectService, private containerService: ContainerService, private projectStore: Store<ProjectsState>, private containerStore: Store<ContainerState>) { }

    resolve(route: ActivatedRouteSnapshot): any {
        return this.projectService.getProjectByUid(route.params.id).subscribe(project => {
            this.projectStore.dispatch(new projectActions.UpdateProjectSuccess(project));
            this.containerService.getAllContainers(route.params.id)//
                .subscribe(res => {
                    this.containerStore.dispatch(new containerActions.LoadAllContainersSuccess(res));
                    this.containerStore.dispatch(new containerActions.LoadActiveContainer(res[0]));
                });
        });
    }
}
