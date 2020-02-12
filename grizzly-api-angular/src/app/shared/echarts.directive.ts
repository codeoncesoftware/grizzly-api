import { Directive, ElementRef, Input, HostListener, AfterViewInit, OnDestroy, OnChanges, OnInit } from '@angular/core';
import echarts from 'echarts';
import 'echarts/theme/macarons';
import { DashboardService } from '../layout/dashboard/dashboard.service';
import { Store } from '@ngrx/store';
import { DashboardState } from '../store/dashboard/dashboard.state';

@Directive({ selector: '[myECharts]' })

export class EChartsDirective implements OnInit, OnDestroy, OnChanges {

  el: ElementRef;
  constructor(el: ElementRef, private dashboardService: DashboardService, private store: Store<DashboardState>) {
    this.el = el;
    // this.subscription = layoutService.echartsState$.subscribe((state) => {
    //   this.resizeChart(state);
    // });
  }

  @Input() EChartsOptions: any;
  private myChart;
  private analyticsData;

  ngOnInit(): void {
    this.store.select('dashboard').subscribe(state => {
      setTimeout(() => {
        this.myChart = echarts.init(this.el.nativeElement, 'macarons');
        if (!this.EChartsOptions) { return; }
        this.myChart.setOption(this.EChartsOptions);
      }, 10);
    });
  }

  ngOnChanges() {
    setTimeout(() => { // Important
      if (this.myChart) {
        this.myChart.update();
      }
    }, 300);
  }

  ngOnDestroy() {
    if (this.myChart) {
      this.myChart.dispose();
      this.myChart = null; // https://bitbucket.org/iarouse/angular-material/commits/5eec2667b5496edfa1cc0896333b83e188a35676
    }
  }

  resizeChart = (state) => {
    // console.log('state: ' + state)
    setTimeout(() => { // Important
      if (this.myChart) {
        this.myChart.resize();
      }
    }, 300);
  }

  @HostListener('window:resize')
  onResize() {
    this.resizeChart(true);
  }
}
