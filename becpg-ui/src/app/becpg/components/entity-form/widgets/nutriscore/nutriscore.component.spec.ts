import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NutriscoreComponent } from './nutriscore.component';

describe('NutriscoreComponent', () => {
  let component: NutriscoreComponent;
  let fixture: ComponentFixture<NutriscoreComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NutriscoreComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NutriscoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
