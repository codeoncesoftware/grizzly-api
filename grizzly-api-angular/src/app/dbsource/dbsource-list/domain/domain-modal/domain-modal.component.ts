import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-domain-modal',
  templateUrl: './domain-modal.component.html',
  styleUrls: ['./domain-modal.component.scss']
})
export class DomainModalComponent implements OnInit {



  constructor(public dialogRef: MatDialogRef<DomainModalComponent>) { }

  ngOnInit() {

  }

  createNewDataSource() {

  }

}
