import { ErrorHandler, Injectable, Injector } from '@angular/core';
import { LoggingService } from '../services/logger-service';
import { HttpErrorResponse } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

    constructor(private injector: Injector, private toastr: ToastrService) {

    }
    handleError(error) {
        const logger = this.injector.get(LoggingService);

        // TODO : add some code to handle errors

        throw error;
    }
}


