import { Component, OnInit } from '@angular/core';
import {Color} from 'ng2-charts/ng2-charts';

interface Items {
  id: number;
  datasource: string;
  name: string;
  labels: string;
  queries: string;
  status: string;
  starttime: Date;
  count: number;
  total: number;
}

const MONITOR_DATA: Items[] = [
  {
    id: 101,
    datasource: 'northwind',
    name: 'monitor #1',
    labels: 'order',
    queries: 'query of northwind',
    status: 'held',
    starttime: new Date('2019-01-01'),
    count: 10,
    total: 100
  },
  {
    id: 201,
    datasource: 'northwind',
    name: 'monitor #2',
    labels: 'customer',
    queries: 'query of northwind',
    status: 'progress',
    starttime: new Date('2019-01-02'),
    count: 10,
    total: 100
  },
  {
    id: 301,
    datasource: 'airroutes',
    name: 'monitor #3',
    labels: 'airport',
    queries: 'query of airroutes',
    status: 'held',
    starttime: new Date('2019-03-01'),
    count: 10,
    total: 100
  },
  {
    id: 401,
    datasource: 'airroutes',
    name: 'monitor #4',
    labels: 'route',
    queries: 'query of airroutes',
    status: 'canceled',
    starttime: new Date('2019-03-02'),
    count: 10,
    total: 100
  },
  {
    id: 501,
    datasource: 'sample',
    name: 'monitor #5',
    labels: 'person',
    queries: 'query of sample',
    status: 'completed',
    starttime: new Date('2019-05-01'),
    count: 10,
    total: 100
  },
];

@Component({
  selector: 'app-monitor-list',
  templateUrl: './monitor-list.component.html',
  styleUrls: ['./monitor-list.component.scss']
})
export class MonitorListComponent implements OnInit {

  heading = 'Monitor List';
  subheading = 'This is an real-time monitor list for Agenspop.';
  icon = 'pe-7s-plane icon-gradient bg-tempting-azure';

  slideConfig6 = {
    className: 'center',
    infinite: true,
    slidesToShow: 1,
    speed: 500,
    adaptiveHeight: true,
    dots: true,
  };

  items = MONITOR_DATA;
  page = 1;

  public datasets = [
    {
      label: 'My First dataset',
      data: [65, 59, 80, 81, 46, 55, 38, 59, 80],
      datalabels: {
        display: false,
      },
    }
  ];

  public datasets2 = [
    {
      label: 'My First dataset',
      data: [46, 55, 59, 80, 81, 38, 65, 59, 80],
      datalabels: {
        display: false,
      },
    }
  ];

  public datasets3 = [
    {
      label: 'My First dataset',
      data: [65, 59, 80, 81, 55, 38, 59, 80, 46],
      datalabels: {
        display: false,
      },
    }
  ];

  public lineChartColors: Color[] = [
    { // dark grey
      backgroundColor: 'rgba(247, 185, 36, 0.2)',
      borderColor: '#f7b924',
      borderCapStyle: 'round',
      borderDash: [],
      borderWidth: 4,
      borderDashOffset: 0.0,
      borderJoinStyle: 'round',
      pointBorderColor: '#f7b924',
      pointBackgroundColor: '#fff',
      pointHoverBorderWidth: 4,
      pointRadius: 6,
      pointBorderWidth: 5,
      pointHoverRadius: 8,
      pointHitRadius: 10,
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: '#f7b924',
    },
  ];

  public lineChartColors2: Color[] = [
    { // dark grey
      backgroundColor: 'rgba(48, 177, 255, 0.2)',
      borderColor: '#30b1ff',
      borderCapStyle: 'round',
      borderDash: [],
      borderWidth: 4,
      borderDashOffset: 0.0,
      borderJoinStyle: 'round',
      pointBorderColor: '#30b1ff',
      pointBackgroundColor: '#ffffff',
      pointHoverBorderWidth: 4,
      pointRadius: 6,
      pointBorderWidth: 5,
      pointHoverRadius: 8,
      pointHitRadius: 10,
      pointHoverBackgroundColor: '#ffffff',
      pointHoverBorderColor: '#30b1ff',
    },
  ];

  public lineChartColors3: Color[] = [
    { // dark grey
      backgroundColor: 'rgba(86, 196, 121, 0.2)',
      borderColor: '#56c479',
      borderCapStyle: 'round',
      borderDash: [],
      borderWidth: 4,
      borderDashOffset: 0.0,
      borderJoinStyle: 'round',
      pointBorderColor: '#56c479',
      pointBackgroundColor: '#fff',
      pointHoverBorderWidth: 4,
      pointRadius: 6,
      pointBorderWidth: 5,
      pointHoverRadius: 8,
      pointHitRadius: 10,
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: '#56c479',
    },
  ];

  public labels = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August'];

  public options = {
    layout: {
      padding: {
        left: 0,
        right: 8,
        top: 0,
        bottom: 0
      }
    },
    scales: {
      yAxes: [{
        ticks: {
          display: false,
          beginAtZero: true
        },
        gridLines: {
          display: false
        }
      }],
      xAxes: [{
        ticks: {
          display: false
        },
        gridLines: {
          display: false
        }
      }]
    },
    legend: {
      display: false
    },
    responsive: true,
    maintainAspectRatio: false
  };

  ngOnInit() {
  }
}
