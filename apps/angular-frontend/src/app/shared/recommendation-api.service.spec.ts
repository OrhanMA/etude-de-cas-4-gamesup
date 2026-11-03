import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RecommendationApiService } from './recommendation-api.service';

describe('RecommendationApiService', () => {
  let api: RecommendationApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(RecommendationApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads recommendations from the authenticated Spring route', () => {
    const response = { strategy: 'knn-hybrid', recommendations: [] } as const;
    api.getRecommendations().subscribe((result) => expect(result).toEqual(response));

    const request = http.expectOne('/api/recommendations');
    expect(request.request.method).toBe('GET');
    request.flush(response);
  });
});
