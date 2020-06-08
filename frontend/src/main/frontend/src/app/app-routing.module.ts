import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
// import { DashboardComponent } from './dashboard/dashboard.component';
// import { MonitorComponent } from './monitor/monitor.component';

// Monitor of Agenspop
import { MonitorListComponent } from './monitor/monitor-list/monitor-list.component';
import { MonitorViewComponent } from './monitor/monitor-view/monitor-view.component';
import { MonitorLayoutComponent } from './monitor/monitor-layout/monitor-layout.component';
import { MonitorRealtimeComponent } from './monitor/monitor-realtime/monitor-realtime.component';


const routes: Routes = [
  {
    path: 'monitor',
    component: MonitorLayoutComponent,
    children: [
      {path: '', component: MonitorListComponent, data: {extraParameter: 'dashboardsMenu'}},
      {path: 'view', component: MonitorViewComponent, data: {extraParameter: 'elementsMenu'}},
      {path: 'realtime', component: MonitorRealtimeComponent, data: {extraParameter: 'elementsMenu'}},
    ]
  },
  {path: '**', redirectTo: '/monitor'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes,
    {
      scrollPositionRestoration: 'enabled',
      anchorScrolling: 'enabled',
    })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
