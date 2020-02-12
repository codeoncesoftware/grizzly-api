import * as jQuery from 'jquery';
import { Component, OnInit } from '@angular/core';

import { APPCONFIG } from './config';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'grizzlyApiAngular';
  public AppConfig: any;


  ngOnInit() {
    this.AppConfig = APPCONFIG;
  }

}
