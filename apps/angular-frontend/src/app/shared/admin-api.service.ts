import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Author, Order, Publisher, SaveOrderLine, SaveUserRequest, UpdateOrderRequest, User } from './api.models';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly http = inject(HttpClient);

  getUsers() { return this.http.get<User[]>('/api/users'); }
  getUser(id: number) { return this.http.get<User>(`/api/users/${id}`); }
  updateUser(id: number, user: SaveUserRequest) { return this.http.put<void>(`/api/users/${id}`, user); }
  deleteUser(id: number) { return this.http.delete<void>(`/api/users/${id}`); }

  getOrders() { return this.http.get<Order[]>('/api/orders'); }
  getOrder(id: number) { return this.http.get<Order>(`/api/orders/${id}`); }
  updateOrder(id: number, request: UpdateOrderRequest) { return this.http.put<void>(`/api/orders/${id}`, request); }
  deleteOrder(id: number) { return this.http.delete<void>(`/api/orders/${id}`); }

  createAuthor(name: string) { return this.http.post<void>('/authors', { name }); }
  updateAuthor(id: number, name: string) { return this.http.put<void>(`/authors/${id}`, { name }); }
  deleteAuthor(id: number) { return this.http.delete<void>(`/authors/${id}`); }

  createPublisher(name: string) { return this.http.post<void>('/publishers', { name }); }
  updatePublisher(id: number, name: string) { return this.http.put<void>(`/publishers/${id}`, { name }); }
  deletePublisher(id: number) { return this.http.delete<void>(`/publishers/${id}`); }

  saveOrderLines(id: number, current: Pick<Order, 'paid' | 'delivered' | 'archived'>, lines: SaveOrderLine[]) {
    return this.updateOrder(id, { ...current, lines });
  }
}
