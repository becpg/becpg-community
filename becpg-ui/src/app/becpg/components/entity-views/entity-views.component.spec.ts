import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityViewsComponent } from './entity-views.component';

describe('EntityViewsComponent', () => {
  let component: EntityViewsComponent;
  let fixture: ComponentFixture<EntityViewsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityViewsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityViewsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
