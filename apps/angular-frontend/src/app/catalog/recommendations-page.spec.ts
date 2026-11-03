import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthApiService } from '../auth/auth-api.service';
import { RecommendationApiService } from '../shared/recommendation-api.service';
import { RecommendationsPage } from './recommendations-page';

describe('RecommendationsPage', () => {
  let fixture: ComponentFixture<RecommendationsPage>;
  let api: { getRecommendations: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    api = { getRecommendations: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [RecommendationsPage],
      providers: [
        provideRouter([]),
        { provide: AuthApiService, useValue: { isAdmin: () => false, logout: vi.fn() } },
        { provide: RecommendationApiService, useValue: api },
      ],
    }).compileComponents();
  });

  it('affiche les jeux renvoyés par Spring et le score de proximité', () => {
    api.getRecommendations.mockReturnValue(of({
      strategy: 'knn-hybrid',
      recommendations: [{
        score: 0.91,
        game: {
          id: 12,
          name: 'Jeu conseillé',
          genre: 'Stratégie',
          editionNumber: 1,
          price: 24.9,
          author: null,
          publisher: null,
        },
      }],
    }));

    fixture = TestBed.createComponent(RecommendationsPage);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Jeu conseillé');
    expect(fixture.nativeElement.textContent).toContain('91%');
  });

  it('affiche un message de reprise lorsque Spring ne fournit pas les recommandations', () => {
    api.getRecommendations.mockReturnValue(throwError(() => new Error('Service indisponible')));

    fixture = TestBed.createComponent(RecommendationsPage);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent)
      .toContain('Les recommandations ne sont pas disponibles');
  });
});
