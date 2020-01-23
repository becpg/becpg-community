import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityRecentsComponent } from './entity-recents.component';

describe('EntityRecentsComponent', () => {
  let component: EntityRecentsComponent;
  let fixture: ComponentFixture<EntityRecentsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityRecentsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityRecentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
