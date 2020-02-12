import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MainLayoutComponent } from './main-layout.component';
import { ProjectsResolver } from '../../shared/resolvers/all-projects.resolver';
import { ActiveProjectResolver } from 'src/app/shared/resolvers/active-project.resolver';
import { DBSourcesResolver } from 'src/app/shared/resolvers/all-datasources';
import { ActiveDBSourceResolver } from 'src/app/shared/resolvers/active-dbsource.resolver';
import { DashboardComponent } from '../dashboard/dashboard.component';
import { NotFoundComponent } from '../not-found/not-found.component';
import { SettingsComponent } from '../settings/settings.component';
import { DashboardResolver } from 'src/app/shared/resolvers/dashboard.resolver';

const routes: Routes = [
  {
    path: 'app',
    component: MainLayoutComponent,
    resolve: { projectsList: ProjectsResolver, dbsourcesList: DBSourcesResolver },
    children: [
      { path: '', redirectTo: '/app/dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent, resolve: { dashboard: DashboardResolver } },
      { path: 'settings', component: SettingsComponent },
      { path: 'project', loadChildren: '../../project/project.module#ProjectModule' },
      { path: 'dbsource', loadChildren: '../../dbsource/dbsource.module#DBSourceModule' },
      { path: '**', component: NotFoundComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { enableTracing: false })],
  exports: [RouterModule],
  providers: [ProjectsResolver, ActiveProjectResolver, DBSourcesResolver, ActiveDBSourceResolver, DashboardResolver]
})
export class MainLayoutRoutingModule { }
