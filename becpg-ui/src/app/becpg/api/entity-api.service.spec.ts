import { TestBed } from '@angular/core/testing';

import { EntityApiService } from './entity-api.service';

describe('EntityApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: EntityApiService = TestBed.get(EntityApiService);
    expect(service).toBeTruthy();
  });
});
