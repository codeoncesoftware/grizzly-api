import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ResourceGroupComponent } from './resource-group.component';

const routes: Routes = [
  {
    path: '',
    component: ResourceGroupComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ResourceGroupRoutingModule { }
