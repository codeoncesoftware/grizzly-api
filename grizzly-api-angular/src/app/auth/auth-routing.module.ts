import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { ResetPasswordResolver } from '../shared/resolvers/reset-password.resolver';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'login', component: LoginComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [ResetPasswordResolver]
})
export class AuthRoutingModule { }
