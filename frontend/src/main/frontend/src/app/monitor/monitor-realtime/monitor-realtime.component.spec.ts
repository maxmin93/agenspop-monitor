import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MonitorRealtimeComponent } from './monitor-realtime.component';

describe('MonitorRealtimeComponent', () => {
  let component: MonitorRealtimeComponent;
  let fixture: ComponentFixture<MonitorRealtimeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MonitorRealtimeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MonitorRealtimeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
