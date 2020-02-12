import { MatDialog } from '@angular/material';
import { ImportModalComponent } from './import-modal.component';

export class ImportModalService {

    constructor(public dialog: MatDialog) { }

    openImport(params) {
        return this.dialog.open(ImportModalComponent,
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
