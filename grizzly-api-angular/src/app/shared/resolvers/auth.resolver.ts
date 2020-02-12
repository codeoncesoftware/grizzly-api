import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from 'src/app/auth/auth.service';
import { Injectable } from '@angular/core';

@Injectable()
export class AuthResolver implements Resolve<any> {
    constructor(private authService: AuthService, private router: Router) { }

    resolve(route: ActivatedRouteSnapshot): any {
        // return this.authService.confirmEmail(route.params.token).subscribe(() => {
        //     this.router.navigateByUrl('/login');
        // });
    }
}
