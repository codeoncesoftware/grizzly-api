import { Component, OnInit } from '@angular/core';
import { DashboardService } from './dashboard.service';
import { DashboardState } from 'src/app/store/dashboard/dashboard.state';
import { Store } from '@ngrx/store';
import { Analytic } from 'src/app/shared/models/Analytic';
import { AppTranslateService } from 'src/app/shared/services/app-translate-service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  public apiCountChart: any;
  public dataChart: any;

  public analyticsData: Analytic;

  constructor(private dashboardService: DashboardService, private store: Store<DashboardState>, private appTranslateService: AppTranslateService) {
    // Set i18n Language
    this.appTranslateService.setDefaultLang(localStorage.getItem('grizzly-lang'));
  }

  ngOnInit() {

    this.store.select('dashboard').subscribe(state => {
      this.analyticsData = state.analytics;
      const apiCounts = this.analyticsData.apiCounts;
      if (apiCounts) {
        this.apiCountChart = this.dashboardService.getApiCountGraph(
          this.dashboardService.count(apiCounts, 'Query'),
          this.dashboardService.count(apiCounts, 'Thymeleaf'),
          this.dashboardService.count(apiCounts, 'FreeMarker'),
          this.dashboardService.count(apiCounts, 'XSL'));

        //
        const dataSize = this.analyticsData.data;
        this.dataChart = this.dashboardService.getDataUploadChart(dataSize.storedContent, dataSize.storedFile);

      }
    });
  }

}
