import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityViewToolbarComponent } from './entity-view-toolbar.component';

describe('EntityViewToolbarComponent', () => {
  let component: EntityViewToolbarComponent;
  let fixture: ComponentFixture<EntityViewToolbarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityViewToolbarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityViewToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
