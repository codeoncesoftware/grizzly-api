import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { MessageModalComponent } from './message-modal.component';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  private dialogRef: MatDialogRef<MessageModalComponent>;

  constructor(public dialog: MatDialog) { }

  openError(titleKey: string, msgKey: string = '', params: {}, subtitle?: string) {
    return this.openConfirm(titleKey, msgKey, params, 0, subtitle);
  }

  openWarning(titleKey: string, msgKey: string = '', params: {}, subtitle?: string) {
    return this.openConfirm(titleKey, msgKey, params, 1, subtitle);
  }

  openInfo(titleKey: string, msgKey: string = '', params: {}, subtitle?: string) {
    return this.openConfirm(titleKey, msgKey, params, 2, subtitle);
  }

  openConfirm(titleKey: string, msgKey: string = '', params: {}, mode: number, subtitle?: string) {
    this.dialogRef = this.dialog.open(MessageModalComponent,
      {
        width: '50%',
        position: {
          top: '200px',
        },
        hasBackdrop: true,
        data: {
          title: titleKey,
          subtitle,
          message: msgKey,
          params,
          mode
        },
        panelClass: 'custom-dialog-container'
      });
    return this.dialogRef;
  }
}

// How to Use IT
// this.messageModalService.openWarning('title', 'message', {}).afterClosed().subscribe(() => console.log('rr'));
// private messageModalService: MessageService
