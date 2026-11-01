import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { OrdersApiService } from './orders-api.service';

describe('OrdersApiService', () => {
  let api: OrdersApiService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(OrdersApiService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('creates an order with game identifiers and quantities only', () => {
    api.create([{ gameId: 7, quantity: 2 }]).subscribe();
    const request = httpTestingController.expectOne('/api/orders');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ lines: [{ gameId: 7, quantity: 2 }] });
    expect(request.request.body.userId).toBeUndefined();
    expect(request.request.body.lines[0].unitPrice).toBeUndefined();
    request.flush({ id: 12, lines: [] });
  });

  it('requests only the authenticated customer orders endpoint', () => {
    api.getMine().subscribe();
    const request = httpTestingController.expectOne('/api/orders/mine');

    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('requests an individual order through the owner-scoped endpoint', () => {
    api.getMineById(12).subscribe();
    const request = httpTestingController.expectOne('/api/orders/mine/12');

    expect(request.request.method).toBe('GET');
    request.flush({ id: 12, lines: [] });
  });
});
