import { FormControl, FormGroupDirective, NgForm } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';

export class MyErrorStateMatcher implements ErrorStateMatcher {
    isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
        // tslint:disable-next-line: no-string-literal
        return control.parent.errors && control.dirty && control.parent.errors['notSame'];
    }
}
