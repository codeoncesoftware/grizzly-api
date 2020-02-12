import { Component, OnInit, Inject, ElementRef, AfterViewInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-execute-modal',
  templateUrl: './execute-modal.component.html',
  styleUrls: ['./execute-modal.component.scss']
})
export class ExecuteModalComponent implements OnInit, AfterViewInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
  }

  ngAfterViewInit() {
    // tslint:disable-next-line: no-string-literal
    document.querySelector('#content')['contentWindow'].document.write(this.data.content);
  }
}
