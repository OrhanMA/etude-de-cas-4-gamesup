import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CatalogPage } from './catalog-page';

describe('CatalogPage ordering flow', () => {
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CatalogPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('submits only game IDs and quantities, then confirms the created order', () => {
    const fixture = TestBed.createComponent(CatalogPage);
    fixture.detectChanges();
    httpTestingController.expectOne('/games').flush([{
      id: 7,
      name: 'Catan',
      genre: 'Stratégie',
      editionNumber: 1,
      price: 29.9,
      author: null,
      publisher: null,
    }]);
    fixture.detectChanges();

    const addButton = fixture.nativeElement.querySelector('[aria-label="Ajouter Catan au panier"]') as HTMLButtonElement;
    addButton.click();
    fixture.detectChanges();
    const checkoutButton = fixture.nativeElement.querySelector('.checkout-button') as HTMLButtonElement;
    checkoutButton.click();

    const request = httpTestingController.expectOne('/api/orders');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ lines: [{ gameId: 7, quantity: 1 }] });
    request.flush({ id: 42, lines: [] });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Commande n° 42 créée');
  });
});
