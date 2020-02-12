import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { ENTER, COMMA, SPACE } from '@angular/cdk/keycodes';
import { MatChipInputEvent } from '@angular/material';

@Component({
  selector: 'app-resource-projection',
  templateUrl: './resource-projection.component.html',
  styleUrls: ['./resource-projection.component.scss']
})

export class ResourceProjectionComponent implements OnInit {

  @Output() fieldsUpdated: EventEmitter<string[]> = new EventEmitter();
  @Input() fields: string[];
  visible = true;
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA, SPACE];

  constructor() { }

  ngOnInit() {
    // Check if the Fields Input is undefined
    if (!this.fields) {
      this.fields = [];
    }
  }

  add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;
    // Add our fruit
    if ((value || '').trim()) {
      this.fields.push(value.trim());

      this.fieldsUpdated.emit(this.fields);
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }
  }

  remove(field: string): void {
    const index = this.fields.indexOf(field);

    if (index >= 0) {
      this.fields.splice(index, 1);
    }

    this.fieldsUpdated.emit(this.fields);
  }

}
