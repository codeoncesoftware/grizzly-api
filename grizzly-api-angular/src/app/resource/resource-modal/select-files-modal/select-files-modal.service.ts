import { MatDialog } from '@angular/material';
import { SelectFilesModalComponent } from './select-files-modal.component';
import { EventEmitter } from '@angular/core';
import { FileNode } from 'src/app/container/import-modal/import-modal.component';
import { SelectedFile } from 'src/app/shared/models/SelectedFile';

export class SelectFilesModalService {

    public selectedFile = new EventEmitter<SelectedFile>();

    constructor(public dialog: MatDialog) { }

    openModal(params) {
        return this.dialog.open(SelectFilesModalComponent,
            {
                width: '50%',
                height: '75vh',
                position: {
                  top: '15vh'
                },
                data: params,
                hasBackdrop: true
            });

    }
}
