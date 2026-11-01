import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CatalogApiService } from './catalog-api.service';

describe('CatalogApiService', () => {
  let api: CatalogApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(CatalogApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('covers catalogue search, details, and the game CRUD routes', () => {
    const game = { name: 'Catan', genre: 'Stratégie', editionNumber: 1, price: 24.9, authorId: null, publisherId: null };
    api.getGames().subscribe();
    api.searchGames('stratégie').subscribe();
    api.getGame(5).subscribe();
    api.createGame(game).subscribe();
    api.updateGame(5, game).subscribe();
    api.deleteGame(5).subscribe();

    const all = http.expectOne((request) => request.url === '/games' && request.method === 'GET');
    const search = http.expectOne((request) => request.url === '/games/search' && request.method === 'GET');
    const detail = http.expectOne((request) => request.url === '/games/5' && request.method === 'GET');
    const create = http.expectOne((request) => request.url === '/games' && request.method === 'POST');
    const update = http.expectOne((request) => request.url === '/games/5' && request.method === 'PUT');
    const remove = http.expectOne((request) => request.url === '/games/5' && request.method === 'DELETE');
    expect(search.request.params.get('query')).toBe('stratégie');
    expect(create.request.body).toEqual(game);
    expect(update.request.body).toEqual(game);
    for (const request of [all, search, detail, create, update, remove]) request.flush(null);
  });

  it('covers author and publisher list and detail routes', () => {
    api.getAuthors().subscribe();
    api.getAuthor(2).subscribe();
    api.getPublishers().subscribe();
    api.getPublisher(4).subscribe();

    for (const url of ['/authors', '/authors/2', '/publishers', '/publishers/4']) {
      const request = http.expectOne(url);
      expect(request.request.method).toBe('GET');
      request.flush(null);
    }
  });
});
