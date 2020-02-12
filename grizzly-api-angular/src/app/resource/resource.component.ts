import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Resource } from '../shared/models/Resource';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmModalComponent } from '../shared/confirm-modal/confirm-modal.component';
import { ResourceService } from './resource.service';
import { resource } from 'selenium-webdriver/http';

@Component({
  selector: 'app-resource',
  templateUrl: './resource.component.html',
  styleUrls: ['./resource.component.scss']
})
export class ResourceComponent implements OnInit {

  @Input() resource: Resource;

  @Output() deleted = new EventEmitter<string>();

  constructor(public dialog: MatDialog, private resourceService: ResourceService) { }

  ngOnInit() {

  }

  public openConfirmDeleteDialog(uuid: string) {
    const dialogRef = this.dialog.open(ConfirmModalComponent,
      {
        width: '40%',
        position: {
          top: '15vh'
        },
        data: {
          action: {
            msg: 'Delete this Group Resource'
          }
        }
      });
    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.resourceService.deleteResourceByUuid(uuid).subscribe(() => this.emitDeletedResource(uuid));
      }
    });
  }

  emitDeletedResource(uuid: string) {
    this.deleted.emit(uuid);
  }
  openResourceDetailsDialog(id: string) { }

}

