import { TestBed } from '@angular/core/testing';

import { EntityFormService } from './entity-form.service';

describe('EntityFormService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: EntityFormService = TestBed.get(EntityFormService);
    expect(service).toBeTruthy();
  });
});
