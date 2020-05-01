import {Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-monitor-title',
  templateUrl: './monitor-title.component.html',
})
export class MonitorTitleComponent {

  @Input() heading;
  @Input() subheading;
  @Input() icon;

  @Output() refreshEmitter= new EventEmitter<boolean>();

  clickRefresh(){
    this.refreshEmitter.emit(true);
  }
}
