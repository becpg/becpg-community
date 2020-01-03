import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityDatagridComponent } from './entity-datagrid.component';

describe('EntityDatagridComponent', () => {
  let component: EntityDatagridComponent;
  let fixture: ComponentFixture<EntityDatagridComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityDatagridComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityDatagridComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
