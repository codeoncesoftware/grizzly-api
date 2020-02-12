import { Component, OnInit, Input } from '@angular/core';
import { Resource } from 'src/app/shared/models/Resource';
import { Parameter } from 'src/app/shared/models/Parameter';

@Component({
  selector: 'app-recap',
  templateUrl: './recap.component.html',
  styleUrls: ['./recap.component.sass']
})
export class RecapComponent implements OnInit {

  @Input() resource: Resource;
  @Input() secondaryFiles: any[];
  @Input() parameters = [] as Parameter[];
  @Input() selectedFileName: string;

  constructor() {
  }

  ngOnInit() {
  }

}
