import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityLogoComponent } from './entity-logo.component';

describe('EntityLogoComponent', () => {
  let component: EntityLogoComponent;
  let fixture: ComponentFixture<EntityLogoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityLogoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityLogoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
