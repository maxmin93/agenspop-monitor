import { Component, ViewChild, ElementRef, NgZone, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router }     from '@angular/router';
import { Observable }         from 'rxjs';
import { map }                from 'rxjs/operators';

import { AmApiService } from '../../services/am-api.service';
import { IAggregation, IQuery } from '../../services/agens-event-types';
import { DATE_UTILS } from '../../services/agens-util-funcs';

import { NgbModal, ModalDismissReasons, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import * as _ from 'lodash';

import { IGraph, EMPTY_GRAPH, IElement, IUserEvent, ILabels, ILabel } from '../../services/agens-graph-types';
import { CY_STYLES } from '../../services/agens-cyto-styles';

import * as am4core from "@amcharts/amcharts4/core";
import * as am4charts from "@amcharts/amcharts4/charts";
import am4themes_animated from "@amcharts/amcharts4/themes/animated";

am4core.useTheme(am4themes_animated);

declare const cytoscape:any;
declare const tippy:any;
declare const jQuery:any;

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
};

export interface ICtxMenuItem {
  label: string;
  click: Function;
  subActions?: ICtxMenuItem[];
};

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

  private cyPrevEvent:IUserEvent = { type: undefined, data: undefined };  // 중복 idle 이벤트 제거용

  // for doubleTap
  tappedBefore:any;
  tappedTimeout:any;
  tappedTarget:any;
  tappedCount:number = 0;

  heading = 'Monitor Dashboard';
  subheading = 'This is an real-time monitor dashboard for Agenspop.';
  icon = 'pe-7s-plane icon-gradient bg-tempting-azure';

  @ViewChild("cy", {read: ElementRef, static: false}) divCy: ElementRef;
  // @Output() actionEmitter= new EventEmitter<IUserEvent>();

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
          if( this.query.query ){
            this.query['slicedQry'] = this.query.query.substr(0,100);

            // load graph-data
            this.g = this.loadQueryByGremlin(this.query.datasource, this.query.query);
            console.log("graph:", this.g);
          }
        }
      });
    });
  }

  ngAfterViewInit() {
    this.doInitChart();
  }

  ngOnDestroy() {
    this.doDestoryChart();
  }

  doInitChart(){
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

  doDestoryChart(){
    this.zone.runOutsideAngular(() => {
      if (this.chart) this.chart.dispose();
    });
  }

  doRefresh($event){
    if( $event ){
      this.doDestoryChart();
      this.doInitChart();
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

  /////////////////////////////////////////////////////////////////////////

  /* //
  1) 기간 from ~ to 구하고
  2) event_rows 로부터 조건1(qid), 조건2(from ~ to) 조회
      => ids list (중복제거)
      ** NOTE: alert 서버에서 필요한 데이터를 모두 제공하도록 기술
  3) agenspop 에 질의
      - Lv1 : monitoring target elements
      - Lv2 : connected edges (if targets are nodes) or vertices (if targets are edges)
      - Lv3 : if any target is not edge, retrieve neighbors of targets and connected edges
  4) Styling
      - targets : opacity  = 1, labeling by id
      - others : opacity = 0.3, no labeling
  5) Action
      - On click, show properties of itself
  */ //

  private getLabels(arr:IElement[]):ILabel[] {
    let grp = _.groupBy(arr, 'data.label');     // { labelName: [eles ...], ... }
    let keys = Object.keys(grp);
    let labels:ILabel[] = [];
    for( let i=0; i<keys.length; i+=1 ){
      let eles = grp[keys[i]];
      labels.push( <ILabel>{ idx: i, name: keys[i], size: eles.length, elements: eles } );
    }
    return labels;
  }

  loadQueryByGremlin( datasource:string, script:string ){
    let graph:IGraph = {
      datasource: datasource,
      labels: { nodes: [], edges: [] },
      nodes: [],
      edges: []
    };

    let eles$:Observable<IElement[]> = this.amApiService.execGremlin(datasource, script);
    eles$.subscribe(r=>{
      console.log("** gremlin =>", r);
      let nodes = r.filter(e=>e.group == 'nodes').map(e=>{ e.scratch['type']='targets'; return e; });
      let edges = r.filter(e=>e.group == 'edges').map(e=>{ e.scratch['type']='targets'; return e; });
      if( nodes.length > 0 && edges.length == 0 ){
        graph.nodes = nodes;
        graph.labels.nodes = this.getLabels(nodes);
        // get connected edges of vertices
        let vids = nodes.map(e=>e.data.id);
        let connected$:Observable<IElement[]> = this.amApiService.findConnectedEdges(datasource, vids);
        connected$.subscribe(ce=>{
          console.log("** connected edges =>", ce);
          graph.edges = ce.map(e=>{ e.scratch['type']='neighbors'; return e; });
          graph.labels.edges = this.getLabels(graph.edges);
        });
      }
      else if( nodes.length == 0 && edges.length > 0 ){
        graph.edges = edges;
        graph.labels.edges = this.getLabels(edges);
        // get connected vertices of edges
        let eids = edges.map(e=>e.data.id);
        let connected$:Observable<IElement[]> = this.amApiService.findConnectedVertices(datasource, eids);
        connected$.subscribe(cv=>{
          console.log("** connected vertices =>", cv);
          graph.nodes = cv.map(e=>{ e.scratch['type']='neighbors'; return e; });
          graph.labels.nodes = this.getLabels(graph.nodes);
        });
      }
    });

    return graph;
/*
    .subscribe(result => {
      let labels = result[0];
      let nodesSet:Set<IElement> = new Set();
      for( let i=1; i<results.length; i+=1 ){
        for( let ele of <IElement[]>results[i] ) nodesSet.add( ele );
      }
      let nodes:IElement[] = Array.from(nodesSet.values());
      this.apApiService.findEdgesOfVertices(datasource, nodes.map(e=>e.data.id)).subscribe(edges=>{

        // for DEBUG : elapsedTime recording end
        if( localStorage.getItem('debug')=='true' ){
          console.timeEnd(this.timeLabel);
          console.log(`  => nodes(${(<IElement[]>nodes).length}), edges(${(<IElement[]>edges).length})`);
        }

        // STEP0) make dictionary of nodes, edges
        this.vids = new Map<string,IElement>( (<IElement[]>nodes).map((e,i)=>{
          e.scratch['_idx'] = i;    // for elgrapho
          return [e.data.id, e];
        }) );
        this.eids = new Map<string,IElement>( (<IElement[]>edges).map((e,i)=>{
          return [e.data.id, e];
        }) );

        // STEP1) load nodes and make vids
        this.g = {
          datasource: datasource,
          nodes: nodes,
          edges: undefined,
          labels: undefined
        };
        // STEP2) load edges
        this.g.edges = this.connectedEdges(edges, this.vids);
        // STEP3) load labels
        this.g.labels = {
          nodes: this.getLabels(this.g.nodes, labels['V']),
          edges: this.getLabels(this.g.edges, labels['E'])
        };
        // STEP4) set colors with own label
        this.setColors(this.g.labels);
        // for DEBUG
        window['agens'] = this.g;

        // STEP 5) activate target screen
        this.gCy = this.gEl = this.g;
        if( localStorage.getItem('init-mode')=='canvas' ){
          this.changeScreenMode('canvas');
        } else {
          this.changeScreenMode('webgl');
        }
        // console.log('loadQueryByGremlin', this.gEl);
      });
    });
*/
  }

  /////////////////////////////////////////////////////////////////////////

  private setStyleNode(e:any){
    e.ungrabify();
    if( e.scratch('_color') ){
      e.style('background-color', e.scratch('_color'));
    }
    e.style('background-image', null);
    e.style('border-opacity', 1);
  }
  private setStyleEdge(e:any){
    if( e.scratch('_color') && e.scratch('_color').length == 2 ){
      e.style('target-arrow-color', e.scratch('_color')[1]);
      e.style('line-gradient-stop-colors', e.scratch('_color'));
    }
  }

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

    this.cyInit(config);
  }

  cyInit(config:any){
    cytoscape.warnings(false);                 // ** for PRODUCT : custom wheel sensitive

    if( localStorage.getItem('init-mode')=='canvas' ){
      config.layout = { name: "random"
        , fit: true, padding: 100, randomize: false, animate: false, positions: undefined
        , zoom: undefined, pan: undefined, ready: undefined, stop: undefined
      };
    }
    this.cy = window['cy'] = cytoscape(config);

    // make linking el-events to inner
    this.cy._private.emitter.$customFn = (e)=>this.cyEventsMapper(e);
    ///////////////////////////////
    // register event-handlers
    ///////////////////////////////
    let cy = this.cy;

    // right-button click : context-menu on node
    cy.on('cxttap', (e)=>{
      if( e.target === cy ){
        console.log("cxttap :", <MouseEvent>e.orignalEvent);
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
    // this.actionEmitter.emit(<IUserEvent>{
    //   type: 'property-show',
    //   data: json   //{ index: e.isNode() ? 'v' : 'e', id: e.id() }
    // });

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

  cyNodeDblClick(target:any){
    let e = target.size() > 1 ? target.first() : target;

    // for DEBUG
    if( localStorage.getItem('debug')=='true' ) console.log('DblClick('+this.tappedCount+'):', e.id());

    if( this.tappedTarget == e ) this.tappedCount += 1;
    else{
      this.tappedCount = 1;
      this.tappedTarget = e;
    }

    let eles = this.cy.collection().add(e);
    for( let i=0; i<this.tappedCount; i+=1 ){
      eles = eles.union( eles.neighborhood() );
    }

    eles.grabify();
    eles.select();
  }

  cyBgIdle(target){
    // this.actionEmitter.emit(<IUserEvent>{
    //   type: 'property-hide',
    //   data: this.cyPrevEvent.type !== 'idle'
    // });
    // // reset doubleTap
    if( this.tappedTarget ){
      this.tappedCount = 0;
      this.tappedTarget = null;
    }

    // when label selection, the others set faded
    // ==> release faded style
    this.cy.batch(()=>{   // ==> without triggering redraws
      this.cy.$('node:selected').ungrabify();
      this.cy.nodes().forEach(e=>{ if(e.scratch('_tippy')) e.scratch('_tippy').hide(); });
    });
    this.cy.$(':selected').unselect();
  }

  cyMakeTippy(node, text){
    return tippy( node.popperRef(), {
      content: function(){
        var div = document.createElement('div');
        div.innerHTML = text;
        return div;
      },
      trigger: 'manual',
      arrow: true,
      placement: 'bottom',
      hideOnClick: false,
      multiple: true,
      sticky: true
    } );
  };

}
