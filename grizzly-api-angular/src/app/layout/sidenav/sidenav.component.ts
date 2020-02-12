import { Component, OnInit } from '@angular/core';
import { APPCONFIG } from '../../config';
import { buildInfo} from '../../../generated/build-info';

@Component({
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss']
})
export class SidenavComponent implements OnInit {

  AppConfig;
  buildInfo;


  constructor() { }

  ngOnInit() {
    this.AppConfig = APPCONFIG;
    this.buildInfo = buildInfo;
  }

  toggleCollapsedNav() {
    this.AppConfig.navCollapsed = !this.AppConfig.navCollapsed;
  }

}
