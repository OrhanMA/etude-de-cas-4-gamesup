import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Author, Game, Publisher, SaveGameRequest } from './api.models';

@Injectable({ providedIn: 'root' })
export class CatalogApiService {
  private readonly http = inject(HttpClient);

  getGames() {
    return this.http.get<Game[]>('/games');
  }

  searchGames(query: string) {
    const params = new HttpParams().set('query', query);
    return this.http.get<Game[]>('/games/search', { params });
  }

  getGame(id: number) {
    return this.http.get<Game>(`/games/${id}`);
  }

  getAuthors() {
    return this.http.get<Author[]>('/authors');
  }

  getAuthor(id: number) {
    return this.http.get<Author>(`/authors/${id}`);
  }

  getPublishers() {
    return this.http.get<Publisher[]>('/publishers');
  }

  getPublisher(id: number) {
    return this.http.get<Publisher>(`/publishers/${id}`);
  }

  createGame(game: SaveGameRequest) {
    return this.http.post<void>('/games', game);
  }

  updateGame(id: number, game: SaveGameRequest) {
    return this.http.put<void>(`/games/${id}`, game);
  }

  deleteGame(id: number) {
    return this.http.delete<void>(`/games/${id}`);
  }
}
