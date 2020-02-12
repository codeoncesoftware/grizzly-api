import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from 'src/app/auth/auth.service';

/**
 * Check if the Received Token Exists Already, If Not, Redirect User to the Login Page
 */

@Injectable()
export class ResetPasswordResolver implements Resolve<any> {
    constructor(private router: Router, private authService: AuthService) { }

    resolve(route: ActivatedRouteSnapshot): any {
        // return this.authService.checkToken(route.params.token).subscribe(res => { if (!res) {this.router.navigate(['/login']); } });
    }
}
