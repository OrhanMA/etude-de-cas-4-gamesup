import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RecommendationResponse } from './api.models';

@Injectable({ providedIn: 'root' })
export class RecommendationApiService {
  private readonly http = inject(HttpClient);

  getRecommendations(): Observable<RecommendationResponse> {
    return this.http.get<RecommendationResponse>('/api/recommendations');
  }
}
