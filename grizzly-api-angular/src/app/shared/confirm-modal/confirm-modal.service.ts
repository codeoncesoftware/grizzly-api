import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ConfirmModalComponent } from './confirm-modal.component';

@Injectable({
    providedIn: 'root'
})

export class ConfirmModalService {

    private dialogRef: MatDialogRef<ConfirmModalComponent>;

    constructor(public dialog: MatDialog) { }

    openConfirm(titleKey: string, msgKey: string = '', params: {}) {
        this.dialogRef = this.dialog.open(ConfirmModalComponent,
            {
                width: '50%',
                position: {
                    top: '200px',
                },
                data: {
                    title: titleKey,
                    message: msgKey,
                    params
                }
            });
        return this.dialogRef;
    }
}
