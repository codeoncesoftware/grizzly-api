import { MatDialogRef, MatDialog } from '@angular/material';
import { SwaggerModalComponent } from './swagger-modal.component';

export class SwaggerModalService {

    private dialogRef: MatDialogRef<SwaggerModalComponent>;

    constructor(public dialog: MatDialog) { }

    openModal() {
        this.dialogRef = this.dialog.open(SwaggerModalComponent,
            {
                width: '75%',
                height: '85vh',
                position: {
                },
                hasBackdrop: true,
                data: {
                }
            });
        return this.dialogRef;
    }
}
