import { TestBed } from '@angular/core/testing';

import { EntityDatagridService } from './entity-datagrid.service';

describe('EntityDatagridService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: EntityDatagridService = TestBed.get(EntityDatagridService);
    expect(service).toBeTruthy();
  });
});
