import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MonitorLayoutComponent } from './monitor-layout.component';

describe('MonitorLayoutComponent', () => {
  let component: MonitorLayoutComponent;
  let fixture: ComponentFixture<MonitorLayoutComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MonitorLayoutComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MonitorLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
