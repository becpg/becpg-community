import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityAssociatedProcessesComponent } from './entity-associated-processes.component';

describe('EntityAssociatedProcessesComponent', () => {
  let component: EntityAssociatedProcessesComponent;
  let fixture: ComponentFixture<EntityAssociatedProcessesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityAssociatedProcessesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityAssociatedProcessesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
