import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DbsourceModalComponent } from './dbsource-modal.component';
import { DBSource } from 'src/app/shared/models/DBSource';

@Injectable({
    providedIn: 'root'
})

export class DbSourceModalService {

    private dialogRef: MatDialogRef<DbsourceModalComponent>;

    constructor(public dialog: MatDialog) { }

    openAdd(dbSource: DBSource) {
        return this.openModal(dbSource, 0);
    }

    openupdate(dbSource: DBSource) {
        return this.openModal(dbSource, 1);
    }

    openModal(dbsource: DBSource, mode: number) {
        this.dialogRef = this.dialog.open(DbsourceModalComponent,
            {
                width: '75%',
                height: '80vh',
                hasBackdrop: true,
                data: {
                    dbsource,
                    mode
                }
            });
        return this.dialogRef;
    }
}
