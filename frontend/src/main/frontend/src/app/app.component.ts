import { Component } from '@angular/core';
import { AmApiService } from './services/am-api.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'agens-alert';

  constructor(
    private amApiService: AmApiService,
  ){
    this.amApiService.findProductInfo().subscribe(x=>{
      console.log('** config[agens]:', x);
      localStorage.setItem('agens', x);
      if( x.hasOwnProperty('debug')) localStorage.setItem('debug', x['debug']);
    });
  }
}
