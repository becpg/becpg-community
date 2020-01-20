import { TestBed } from '@angular/core/testing';

import { EntityVersionService } from './entity-version.service';

describe('EntityVersionService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: EntityVersionService = TestBed.get(EntityVersionService);
    expect(service).toBeTruthy();
  });
});
