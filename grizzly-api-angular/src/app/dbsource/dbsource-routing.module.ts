import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DbsourceDetailsComponent } from './dbsource-details/dbsource-details.component';
import { ActiveDBSourceResolver } from '../shared/resolvers/active-dbsource.resolver';

const routes: Routes = [
  {
    path: ':id',
    component: DbsourceDetailsComponent,
    resolve: {active : ActiveDBSourceResolver}
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [ActiveDBSourceResolver]
})
export class DBSourceRoutingModule { }
