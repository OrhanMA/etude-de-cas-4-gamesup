import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Order, SaveOrderLine } from '../shared/api.models';

export type CreateOrderLine = SaveOrderLine;
export type OrderLine = Order['lines'][number];
export type CustomerOrder = Order;

@Injectable({ providedIn: 'root' })
export class OrdersApiService {
  private readonly http = inject(HttpClient);

  getMine() {
    return this.http.get<CustomerOrder[]>('/api/orders/mine');
  }

  getMineById(id: number) {
    return this.http.get<CustomerOrder>(`/api/orders/mine/${id}`);
  }

  create(lines: CreateOrderLine[]) {
    return this.http.post<CustomerOrder>('/api/orders', { lines });
  }
}
