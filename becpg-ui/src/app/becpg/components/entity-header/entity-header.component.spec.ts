import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityHeaderComponent } from './entity-header.component';

describe('EntityHeaderComponent', () => {
  let component: EntityHeaderComponent;
  let fixture: ComponentFixture<EntityHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityHeaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
