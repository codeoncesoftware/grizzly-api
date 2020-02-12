/**
 * In order to use the info section, you need to insert an object into params with this format :
 * info: {
 *        msg: value,
 *        infos: [val1, val2]
 *        }
 * And add the values in i18n
 */
import { Component, OnInit, ViewEncapsulation, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-message-modal',
  templateUrl: './message-modal.component.html',
  styleUrls: ['./message-modal.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class MessageModalComponent implements OnInit {

  mode: number;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
    this.mode = this.data.mode;
  }


}
