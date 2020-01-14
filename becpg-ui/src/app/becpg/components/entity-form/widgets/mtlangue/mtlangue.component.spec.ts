import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MtlangueComponent } from './mtlangue.component';

describe('MtlangueComponent', () => {
  let component: MtlangueComponent;
  let fixture: ComponentFixture<MtlangueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MtlangueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MtlangueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
