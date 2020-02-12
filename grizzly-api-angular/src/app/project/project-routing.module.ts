import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ProjectComponent } from './project.component';
import { ActiveProjectResolver } from '../shared/resolvers/active-project.resolver';
import { ActiveContainerResolver } from '../shared/resolvers/active-container.resolver';

const routes: Routes = [
  {
    path: ':id',
    component: ProjectComponent,
    resolve: {active : ActiveProjectResolver}
  }, {
    path: ':id/:containerId',
    component: ProjectComponent,
    resolve: {active : ActiveContainerResolver}
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [ActiveProjectResolver, ActiveContainerResolver]
})
export class ProjectRoutingModule { }
