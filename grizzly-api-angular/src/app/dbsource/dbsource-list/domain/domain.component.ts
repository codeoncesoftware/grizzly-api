import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DomainModalComponent } from './domain-modal/domain-modal.component';

@Component({
  selector: 'app-domain',
  templateUrl: './domain.component.html',
  styleUrls: ['./domain.component.scss']
})
export class DomainComponent implements OnInit {

  constructor(public dialog: MatDialog) { }

  ngOnInit() {
  }

  public openDomainModal() {
    const dialogRef = this.dialog.open(DomainModalComponent,
      {
        width: '40%',
        position: {
          top: '15vh'
        },
        data: {
          dbsource: null
        }
      });
    dialogRef.afterClosed().subscribe((data) => {
      if (data) {
        alert(data);
      }
    });
  }

}
