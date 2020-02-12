import { MatDialog, MatDialogRef } from '@angular/material';
import { ExecuteModalComponent } from './execute-modal.component';

export class ExecuteModalService {

  private dialogRef: MatDialogRef<ExecuteModalComponent>;

  constructor(public dialog: MatDialog) { }

  openExecuteModal(content) {
    this.dialogRef = this.dialog.open(ExecuteModalComponent,
      {
        width: '90vw',
        height: '90vh',
        position: {

        },
        hasBackdrop: true,
        data: {
          content
        }
      });
    return this.dialogRef;
  }
}
