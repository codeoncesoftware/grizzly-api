import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot } from '@angular/router';
import { ContainerService } from 'src/app/container/container.service';
import { Store } from '@ngrx/store';
import { ContainerState } from 'src/app/store/container/container.state';
import * as containerActions from './../../store/container/container.actions';
import * as projectActions from '../../store/project/project.actions';
import { LayoutState } from 'src/app/store/layout/layout.state';
import { ProjectService } from 'src/app/project/project.service';
import { ProjectsState } from 'src/app/store/project/project.state';


@Injectable()
export class ActiveContainerResolver implements Resolve<any> {
    constructor(private containerService: ContainerService,
                private projectService: ProjectService,
                private containerStore: Store<ContainerState>,
                private projectStore: Store<ProjectsState>) { }

    resolve(route: ActivatedRouteSnapshot): any {
        return this.projectService.getProjectByUid(route.params.id).subscribe(project => {
            this.projectStore.dispatch(new projectActions.UpdateProjectSuccess(project));
            this.containerService.getAllContainers(route.params.id)//
                .subscribe(res => {
                    this.containerStore.dispatch(new containerActions.LoadAllContainersSuccess(res));
                    this.containerService.getContainerByID(route.params.containerId).subscribe(container => {
                        this.containerStore.dispatch(new containerActions.LoadActiveContainer(container));
                    });
                });
        });
    }
}
// return this.containerStore.dispatch(new containerActions.LoadActiveContainerById(route.params.containerId));


// export class ActiveContainerResolver implements Resolve<any> {
//     constructor(private containerService: ContainerService,
//                 private projectService: ProjectService,
//                 private containerStore: Store<ContainerState>,
//                 private projectStore: Store<ProjectsState>) { }

//     resolve(route: ActivatedRouteSnapshot): any {
//         return this.projectService.getProjectByUid(route.params.id).subscribe(project => {
//             this.projectStore.dispatch(new projectActions.UpdateProjectSuccess(project));
//             this.containerService.getAllContainers(route.params.id)//
//                 .subscribe(res => {
//                     this.containerStore.dispatch(new containerActions.LoadAllContainersSuccess(res));
//                     this.containerService.getContainerByID(route.params.containerId).subscribe(container => {
//                         this.containerStore.dispatch(new containerActions.LoadActiveContainer(container));
//                     });
//                 });
//         });
//     }
// }
