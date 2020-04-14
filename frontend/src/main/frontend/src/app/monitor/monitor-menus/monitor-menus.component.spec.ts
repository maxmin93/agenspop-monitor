import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MonitorMenusComponent } from './monitor-menus.component';

describe('MonitorMenusComponent', () => {
  let component: MonitorMenusComponent;
  let fixture: ComponentFixture<MonitorMenusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MonitorMenusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MonitorMenusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
