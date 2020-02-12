import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpHeaders } from '@angular/common/http';
import { Injector, Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
@Injectable({ providedIn: 'root' })
export class HttpErrorInterceptor implements HttpInterceptor {

    constructor(private injector: Injector, private router: Router) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        // set token if it exists
        if (localStorage.getItem('token')) {
            request = request.clone({
                headers: request.headers.set('Authorization', 'Bearer ' + localStorage.getItem('token'))
            });
        }

        // handle response status
        return next.handle(request)
            .pipe(
                catchError((err) => {
                    const toastr = this.injector.get(ToastrService);
                    if (err.status === 302) {
                        // Resource found but it is DUPLICATED
                    } else if (err.status === 400) {
                        toastr.error(err.error, 'this is a 400 ERROR!! Contact the support to report this bug.', {
                            tapToDismiss: true,
                            positionClass: 'toast-top-left',
                            progressBar: true,
                        });
                    } else if (err.status === 401) {
                        if (err.error.error === 'invalid_token') {
                            localStorage.clear();
                            this.router.navigate(['/login']);
                        } else if (err.error.error === 'unauthorized') {
                            localStorage.clear();
                            this.router.navigate(['/login']);
                        } else {
                            // Wrong Credentials
                        }
                    } else if (err.status === 404) {
                        toastr.error(err.error, 'RESOURCE NOT FOUND', {
                            tapToDismiss: true,
                            positionClass: 'toast-top-full-width',
                            progressBar: true,
                        });
                        this.router.navigate(['/app/dashboard']);

                    } else if (err.status === 406) {
                        if (err.error && err.error === 4061) {
                            // Wrong DataSource Details
                        } else {
                            // Transformation with no XML Content
                            toastr.error(err.error, '', {
                                tapToDismiss: true,
                                positionClass: 'toast-top-left',
                                progressBar: true,
                            });
                        }

                    } else { // SERVER ERROR 5**
                        toastr.error('Server cannot be reached', 'COULD NOT CONNECT TO THE SERVER', {
                            tapToDismiss: true,
                            positionClass: 'toast-top-full-width',
                            progressBar: true,
                        });
                    }
                    return throwError(err);
                })
            );
    }
}
