import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityLogoUploadPopupComponent } from './entity-logo-upload-popup.component';

describe('EntityLogoUploadPopupComponent', () => {
  let component: EntityLogoUploadPopupComponent;
  let fixture: ComponentFixture<EntityLogoUploadPopupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityLogoUploadPopupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityLogoUploadPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
