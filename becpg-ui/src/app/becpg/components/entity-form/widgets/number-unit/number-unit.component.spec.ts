import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NumberUnitComponent } from './number-unit.component';

describe('NumberUnitComponent', () => {
  let component: NumberUnitComponent;
  let fixture: ComponentFixture<NumberUnitComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NumberUnitComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NumberUnitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
