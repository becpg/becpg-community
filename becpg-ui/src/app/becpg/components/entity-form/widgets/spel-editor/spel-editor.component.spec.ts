import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SpelEditorComponent } from './spel-editor.component';

describe('SpelEditorComponent', () => {
  let component: SpelEditorComponent;
  let fixture: ComponentFixture<SpelEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SpelEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SpelEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
