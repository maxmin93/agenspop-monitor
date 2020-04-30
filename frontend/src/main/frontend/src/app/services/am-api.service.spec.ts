import { TestBed } from '@angular/core/testing';

import { AmApiService } from './am-api.service';

describe('AmApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AmApiService = TestBed.get(AmApiService);
    expect(service).toBeTruthy();
  });
});
