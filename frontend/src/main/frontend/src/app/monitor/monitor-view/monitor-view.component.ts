import { Component, NgZone, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router }     from '@angular/router';
import { Observable }         from 'rxjs';
import { map }                from 'rxjs/operators';

import { AmApiService } from '../../services/am-api.service';
import { IAggregation, IQuery } from '../../services/agens-event-types';
import { DATE_UTILS } from '../../services/agens-util-funcs';
import * as _ from 'lodash';

import { IGraph, EMPTY_GRAPH, IElement, ILabels, ILabel } from '../../services/agens-graph-types';
import { CY_STYLES } from '../../services/agens-cyto-styles';

import * as am4core from "@amcharts/amcharts4/core";
import * as am4charts from "@amcharts/amcharts4/charts";
import am4themes_animated from "@amcharts/amcharts4/themes/animated";

am4core.useTheme(am4themes_animated);

declare const cytoscape:any;

const CY_CONFIG:any ={
  layout: { name: "euler"
    , fit: true, padding: 50, randomize: true, animate: false, positions: undefined
    , zoom: undefined, pan: undefined, ready: undefined, stop: undefined
  },
  // initial viewport state:
  zoom: 1,
  minZoom: 1e-2,
  maxZoom: 1e1,
  wheelSensitivity: 0.2,
  boxSelectionEnabled: true,
  motionBlur: true,
  selectionType: "single",
  // autoungrabify: true        // cannot move node by user control
}

@Component({
  selector: 'app-monitor-view',
  templateUrl: './monitor-view.component.html',
  styleUrls: ['./monitor-view.component.scss']
})
export class MonitorViewComponent implements OnInit, AfterViewInit {

  aggregations:IAggregation[] = [];
  query:any = { datasource: 'unknown', name: 'unknown', query: null, slicedQry: null };    // IQuery

  qid: number;
  chartData = {
    data: [],
    from: null,
    to: null
  };

  private chart: am4charts.XYChart;

  private g:IGraph = undefined;
  cy: any = undefined;                  // cytoscape.js

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

      this.amApiService.findQuery(this.qid).subscribe(r=>{
        console.log('query:', r);
        if( !!r ){
          this.query = r;
          if( this.query.query ) this.query['slicedQry'] = this.query.query.substr(0,100);
        }
      });
    });
  }

  ngAfterViewInit() {
    this.doInit();
  }

  ngOnDestroy() {
    this.doDestory();
  }

  doInit(){
    let aggregations$ = this.amApiService.findAggregationsByQid(this.qid);
    aggregations$.pipe(map(q=><IAggregation[]>q)).subscribe(rows => {
      this.aggregations = _.sortBy(rows, ['edate']);
      // console.log('aggregations =>', this.aggregations);
      this.chartData = this.makeChartData(this.aggregations);

      this.zone.runOutsideAngular(() =>{
        this.chart = this.initChart(this.chartData);
      });
    });
  }

  doDestory(){
    this.zone.runOutsideAngular(() => {
      if (this.chart) this.chart.dispose();
    });
  }

  doRefresh($event){
    if( $event ){
      this.doDestory();
      this.doInit();
    }
  }

  /////////////////////////////////////////////////////////////////////////

  makeChartData(sorted: IAggregation[]): any {
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


  /////////////////////////////////////////////////////////////////////////
/*
  loadGraph(g:IGraph){
    // for DEBUG
    // if( localStorage.getItem('debug')=='true' ) console.log('loadGraph', g);

    let pan = g.hasOwnProperty('pan') ? g['pan'] : { x:0, y:0 };
    let config:any = Object.assign( _.cloneDeep(CY_CONFIG), {
      container: this.divCy.nativeElement,
      elements: _.concat(g.nodes, g.edges),
      style: CY_STYLES,
      pan: pan,
      ready: (e)=>{
        let cy = e.cy;
        cy.scratch('_datasource', g.datasource);
        cy.nodes().forEach(e => this.setStyleNode(e));
        cy.edges().forEach(e => this.setStyleEdge(e));
      }
    });

    // STEP4) ready event
    this.readyEmitter.emit(<IEvent>{ type: 'node-labels', data: g.labels.nodes });
    this.readyEmitter.emit(<IEvent>{ type: 'edge-labels', data: g.labels.edges });
    this.readyEmitter.emit(<IEvent>{ type: 'layouts', data: this.dispLayouts });

    this.cyInit(config);
  }

  cyInit(config:any){
    cytoscape.warnings(false);                 // ** for PRODUCT : custom wheel sensitive

    // for DEBUG : elapsedTime recording start
    if( localStorage.getItem('debug')=='true' ){
      this.timeLabel = `canvas-ready`;
      console.time(this.timeLabel);
    }

    if( localStorage.getItem('init-mode')=='canvas' ){
      config.layout = { name: "random"
        , fit: true, padding: 100, randomize: false, animate: false, positions: undefined
        , zoom: undefined, pan: undefined, ready: undefined, stop: undefined
      };
    }
    this.cy = window['cy'] = cytoscape(config);

    // **NOTE : 여기서 측정하는 것이 ready()에서 측정하는 것보다 1초+ 정도 느리다.
    //      ==> ready() 에서 모든 nodes, edges 들의 style 처리후 빠져나옴
    //      ==> 이 시점에서 화면상에 그래프는 보이지 않음. 브라우저에서 실제 그리는 시간이 추가로 소요됨 (측정불가. 도구가 없음)

    // for DEBUG : elapsedTime recording end
    if( localStorage.getItem('debug')=='true' ){
      console.timeEnd(this.timeLabel);
      console.log(`  => nodes(${this.cy.nodes().size()}), edges(${this.cy.edges().size()})`);
      this.timeLabel = null;
    }

    // undo-redo
    this.ur = this.cy.undoRedo(UR_CONFIG);
    this.cy.on("afterDo", (event, actionName, args, res)=>{
      this.readyEmitter.emit(<IEvent>{ type: 'undo-changed', data: this.ur });
    });
    this.cy.on("afterUndo", (event, actionName, args, res)=>{
      this.readyEmitter.emit(<IEvent>{ type: 'undo-changed', data: this.ur });
      this.readyEmitter.emit(<IEvent>{ type: 'redo-changed', data: this.ur });
    });
    this.cy.on("afterRedo", (event, actionName, args, res)=>{
      this.readyEmitter.emit(<IEvent>{ type: 'redo-changed', data: this.ur });
    });


    // make linking el-events to inner
    this.cy._private.emitter.$customFn = (e)=>this.cyEventsMapper(e);
    ///////////////////////////////
    // register event-handlers
    ///////////////////////////////
    let cy = this.cy;

    // right-button click : context-menu on node
    cy.on('cxttap', (e)=>{
      if( e.target === cy ){
        this.contextMenuService.show.next({
          anchorElement: cy.popperRef({renderedPosition: () => ({
            x: e.originalEvent.offsetX-5, y: e.originalEvent.offsetY-5 }),}),
          contextMenu: this.cyBgMenu,
          event: <MouseEvent>e.orignalEvent,
          item: e.target
        });
      }
      // **NOTE: ngx-contextmenu is FOOLISH! ==> do change another!
      else if( e.target.isNode() ){
        this.listVertexNeighbors(e.target, ()=>{
          this.contextMenuService.show.next({
            anchorElement: e.target.popperRef(),
            contextMenu: this.cyMenu,
            event: <MouseEvent>e.orignalEvent,
            item: e.target
          });
        });
      }

      e.preventDefault();
      e.stopPropagation();
    });

    // ** 탭 이벤트를 cyEventsMapper()로 전달
    cy.on('tap', (e)=>{
      let tappedNow = event.target;
      if( this.tappedTimeout && this.tappedBefore) {
        clearTimeout(this.tappedTimeout);
      }
      if( this.tappedBefore === tappedNow ){
        e.target.trigger('doubleTap', e);
        e.originalEvent = undefined;
        this.tappedBefore = null;
      }
      else{
        this.tappedTimeout = setTimeout(()=>{
          if( e.target === cy ){    // click background
            cy._private.emitter.$customFn({ type: 'idle', data: e.target });
          }                         // click node or edge
          else if( e.target.isNode() || e.target.isEdge() ){
            cy._private.emitter.$customFn({ type: 'ele-click', data: e.target });
          }
          this.tappedBefore = null;
        }, 300);
        this.tappedBefore = tappedNow;
      }
    });

    // trigger doubleTap event
    // https://stackoverflow.com/a/44160927/6811653
    cy.on('doubleTap', _.debounce( (e, originalTapEvent) => {
      if( e.target !== cy && e.target.isNode() ){
        cy._private.emitter.$customFn({ type: 'node-dblclick', data: e.target });
      }
    }), 500);

    cy.on('boxselect', _.debounce( (e)=>{
      cy.$(':selected').nodes().grabify();
    }), 500);

    cy.on('dragfree','node', (e)=>{
      let pos = e.target.position();
      e.target.scratch('_pos', _.clone(pos));
    });

    cy.on('select','node', (e)=>{
      e.target.style('background-color', '#fff');
      if( !e.target.hasClass('seed')) e.target.style('border-color', e.target.scratch('_color'));
      e.target.style('border-opacity', 1);
      if( !e.target.hasClass('seed')) e.target.style('z-index', 9);
      if( e.target.scratch('_tippy') ) e.target.scratch('_tippy').hide();
    });

    cy.on('unselect','node', (e)=>{
      e.target.ungrabify();
      e.target.style('background-color', e.target.scratch('_color'));
      if( !e.target.hasClass('seed')) e.target.style('border-color', '#fff');
      if( e.target.hasClass('icon')) e.target.style('border-opacity', 0);
      if( !e.target.hasClass('highlighted') && !e.target.hasClass('seed') ) e.target.style('z-index', 0);
      if( e.target.scratch('_tippy') ) e.target.scratch('_tippy').hide();
    });

    // ** node 선택을 위한 편의 기능 (뭉쳤을때)
    cy.on('mouseover', 'node', _.debounce( (e)=>{
      let node = e.target;
      if( node && !node.selected() ){
        if( !node.hasClass('faded') ){        // show
          if( !node.hasClass('highlighted') && !node.hasClass('seed') ) node.style('z-index', 1);
          // node.scratch('_tippy').show();
          setTimeout(()=>{                    // auto-hide
            if( !node.hasClass('highlighted') && node.hasClass('seed') ) node.style('z-index', 0);
          }, 2000);
        }
      }
    }, 200));

  }

  cyEventsMapper(evt:any){
    if( evt.type === 'ele-click' ){
      this.cyElementClick(evt.data);
    }
    else if( evt.type === 'node-dblclick' ){
      this.cyNodeDblClick(evt.data);
    }
    else if( evt.type === 'idle' ){
      this.cyBgIdle(evt.data);
    }
    this.cyPrevEvent = evt;
  }

  cyElementClick(target:any){
    let e = target.size() > 1 ? target.first() : target;
    let json = <IElement>e.json();      // expand 된 개체는 g 모체에 없기 때문에 직접 추출
    json.scratch = e.scratch();         // json() 출력시 scratch 는 누락됨
    this.actionEmitter.emit(<IEvent>{
      type: 'property-show',
      data: json   //{ index: e.isNode() ? 'v' : 'e', id: e.id() }
    });

    if( e.isEdge() ){
      // edge 선택시 연결된 source, target nodes 도 함께 선택
      // ==> mouse drag 가능해짐 (양끝 노드 하나를 붙잡고 이동)
      setTimeout(()=>{
        this.cy.batch(()=>{
          e.source().select();
          e.target().select();
        });
      }, 2);
    }
    // else this.cyHighlight(e);
  }
*/

}
