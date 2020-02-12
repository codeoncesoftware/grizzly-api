import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { ProjectsResolver } from './shared/resolvers/all-projects.resolver';
import { ActiveProjectResolver } from './shared/resolvers/active-project.resolver';
import { ActiveDBSourceResolver } from './shared/resolvers/active-dbsource.resolver';
import { DBSourcesResolver } from './shared/resolvers/all-datasources';
import { AuthResolver } from './shared/resolvers/auth.resolver';
import { LoginComponent } from './auth/login/login.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  { path: 'confirm/email/:token', component: LoginComponent, resolve: {authResolver: AuthResolver}},
  { path: 'app', component: MainLayoutComponent, resolve: {projectsList: ProjectsResolver, dbsourcesList: DBSourcesResolver} },
  { path: 'project', loadChildren: () => import('./project/project.module').then(m => m.ProjectModule), resolve: {active : ActiveProjectResolver} },
  { path: 'dbsource', loadChildren: () => import('./dbsource/dbsource.module').then(m => m.DBSourceModule), resolve: {active: ActiveDBSourceResolver} }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [ActiveProjectResolver, ProjectsResolver, ActiveDBSourceResolver, AuthResolver]
})

export class AppRoutingModule { }
