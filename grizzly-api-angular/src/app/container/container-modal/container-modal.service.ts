import { MatDialog } from '@angular/material';
import { ContainerModalComponent } from './container-modal.component';


export class ContainerModalService {

    constructor(public dialog: MatDialog) { }

    openContainerModal(params) {
        return this.dialog.open(ContainerModalComponent,
            {
                width: '50%',
                position: {
                  top: '15vh'
                },
                data: params
                ,
                hasBackdrop: true
            });

    }
}
