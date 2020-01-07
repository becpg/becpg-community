import { TestBed } from '@angular/core/testing';

import { EntityViewService } from './entity-view.service';

describe('EntityViewService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: EntityViewService = TestBed.get(EntityViewService);
    expect(service).toBeTruthy();
  });
});
