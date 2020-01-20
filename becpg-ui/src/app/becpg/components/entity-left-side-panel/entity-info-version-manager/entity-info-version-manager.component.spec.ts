import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityInfoVersionManagerComponent } from './entity-info-version-manager.component';

describe('EntityInfoVersionManagerComponent', () => {
  let component: EntityInfoVersionManagerComponent;
  let fixture: ComponentFixture<EntityInfoVersionManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityInfoVersionManagerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityInfoVersionManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
