import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AdminApiService } from './admin-api.service';

describe('AdminApiService', () => {
  let api: AdminApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(AdminApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('maps user and order administration to their API routes', () => {
    api.getUsers().subscribe();
    api.getUser(3).subscribe();
    api.updateUser(3, { name: 'Alice', email: 'alice@example.test' }).subscribe();
    api.deleteUser(3).subscribe();
    api.getOrders().subscribe();
    api.getOrder(8).subscribe();
    api.updateOrder(8, { paid: true, delivered: false, archived: false }).subscribe();
    api.deleteOrder(8).subscribe();

    for (const [url, method] of [
      ['/api/users', 'GET'], ['/api/users/3', 'GET'], ['/api/users/3', 'PUT'], ['/api/users/3', 'DELETE'],
      ['/api/orders', 'GET'], ['/api/orders/8', 'GET'], ['/api/orders/8', 'PUT'], ['/api/orders/8', 'DELETE'],
    ]) {
      const request = http.expectOne((candidate) => candidate.url === url && candidate.method === method);
      expect(request.request.method).toBe(method);
      request.flush(null);
    }
  });

  it('maps author and publisher create, update, and delete operations', () => {
    api.createAuthor('Ada').subscribe();
    api.updateAuthor(1, 'Ada Lovelace').subscribe();
    api.deleteAuthor(1).subscribe();
    api.createPublisher('GamesUP').subscribe();
    api.updatePublisher(2, 'GamesUP Studio').subscribe();
    api.deletePublisher(2).subscribe();

    for (const [url, method, body] of [
      ['/authors', 'POST', { name: 'Ada' }], ['/authors/1', 'PUT', { name: 'Ada Lovelace' }], ['/authors/1', 'DELETE', null],
      ['/publishers', 'POST', { name: 'GamesUP' }], ['/publishers/2', 'PUT', { name: 'GamesUP Studio' }], ['/publishers/2', 'DELETE', null],
    ] as const) {
      const request = http.expectOne((candidate) => candidate.url === url && candidate.method === method);
      expect(request.request.method).toBe(method);
      if (body) expect(request.request.body).toEqual(body);
      request.flush(null);
    }
  });
});
