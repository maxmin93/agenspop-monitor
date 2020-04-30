import { Component, NgZone, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router }     from '@angular/router';
import { Observable }         from 'rxjs';
import { map }                from 'rxjs/operators';

import { AmApiService } from '../../services/am-api.service';
import { IAggregations } from '../../services/agens-event-types';
import { DATE_UTILS } from '../../services/agens-util-funcs';
import * as _ from 'lodash';

import * as am4core from "@amcharts/amcharts4/core";
import * as am4charts from "@amcharts/amcharts4/charts";
import am4themes_animated from "@amcharts/amcharts4/themes/animated";

am4core.useTheme(am4themes_animated);

@Component({
  selector: 'app-monitor-view',
  templateUrl: './monitor-view.component.html',
  styleUrls: ['./monitor-view.component.scss']
})
export class MonitorViewComponent implements OnInit, AfterViewInit {

  aggregations:IAggregations[] = [];

  qid: number;
  chartData = {
    data: [],
    from: null,
    to: null
  };

  private chart: am4charts.XYChart;

  heading = 'Monitor Dashboard';
  subheading = 'This is an real-time monitor dashboard for Agenspop.';
  icon = 'pe-7s-plane icon-gradient bg-tempting-azure';

  constructor(
    private amApiService: AmApiService,
    private zone: NgZone,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(){
    let qid:Observable<string> = this.route.queryParamMap.pipe(map(params => params.get('qid')));
    qid.subscribe(q=>{
      console.log('qid:', q);
      if( !q ){   // is null
        this.router.navigate(['']);
        return;
      }
      this.qid = Number.parseInt(q);
    });
  }

  makeChartData(sorted: IAggregations[]): any {
    let chartData : any = {};
    if( _.isNull(sorted) || sorted.length == 0 ) return chartData;

    // get min and max date
    //  - order by edate, qid
    chartData['from'] = sorted[0].edate;
    chartData['to'] = sorted[sorted.length-1].edate;
    chartData['data'] = [];

    // make any array
    //  - loop : min date ~ max date
    let fromDate = new Date(chartData['from'])
    let dayCount = DATE_UTILS.diffDays(fromDate, new Date(chartData['to']));
    for( let idx=0; idx <= dayCount; idx+=1 ){
      let curr = DATE_UTILS.afterDays(fromDate, idx);
      let currString = DATE_UTILS.toYYYYMMDD(curr);
      let row:any = { date: curr, value: 0 };

      let matched = _.find(sorted, r=>r.edate == currString );
      if( matched != undefined ){
        row['value'] = matched.ids_cnt;
      }
      chartData['data'].push(row);
    }
    console.log('chartData:', chartData);
    return chartData;
  }

  initChart(chartData: any):am4charts.XYChart {
    let chart = am4core.create("chartdiv", am4charts.XYChart);
    chart.paddingRight = 20;
    chart.data = chartData.data;

    let dateAxis = chart.xAxes.push(new am4charts.DateAxis());
    dateAxis.renderer.grid.template.location = 0;
    dateAxis.dateFormats.setKey("day", "yyyy-MM-dd");

    let valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
    valueAxis.tooltip.disabled = true;
    valueAxis.renderer.minWidth = 35;

    let series = chart.series.push(new am4charts.LineSeries());
    series.dataFields.dateX = "date";
    series.dataFields.valueY = "value";

    series.tooltipText = "{valueY.value}";
    chart.cursor = new am4charts.XYCursor();

    let scrollbarX = new am4charts.XYChartScrollbar();
    scrollbarX.series.push(series);
    chart.scrollbarX = scrollbarX;

    return chart;
  }

  ngAfterViewInit() {
    let aggregations$ = this.amApiService.findAggregationsByQid(this.qid);
    aggregations$.pipe(map(q=><IAggregations[]>q)).subscribe(rows => {
      this.aggregations = _.sortBy(rows, ['edate']);
      console.log('aggregations =>', this.aggregations);
      this.chartData = this.makeChartData(this.aggregations);

      this.zone.runOutsideAngular(() =>{
        this.chart = this.initChart(this.chartData);
      });
    });
  }

  ngOnDestroy() {
    this.zone.runOutsideAngular(() => {
      if (this.chart) {
        this.chart.dispose();
      }
    });
  }

}
