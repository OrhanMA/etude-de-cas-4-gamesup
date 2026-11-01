export interface Author {
  id: number;
  name: string;
}

export interface Publisher {
  id: number;
  name: string;
}

export interface Game {
  id: number;
  name: string;
  genre: string;
  editionNumber: number;
  price: number | null;
  author: Author | null;
  publisher: Publisher | null;
}

export interface SaveGameRequest {
  name: string;
  genre: string;
  editionNumber: number;
  price: number;
  authorId: number | null;
  publisherId: number | null;
}

export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
}

export interface SaveUserRequest {
  name: string;
  email: string;
}

export interface OrderLine {
  id: number;
  game: Game;
  quantity: number;
  unitPrice: number;
}

export interface Order {
  id: number;
  user: User;
  createdAt: string;
  paid: boolean;
  delivered: boolean;
  archived: boolean;
  lines: OrderLine[];
}

export interface SaveOrderLine {
  gameId: number;
  quantity: number;
}

export interface UpdateOrderRequest {
  paid: boolean;
  delivered: boolean;
  archived: boolean;
  lines?: SaveOrderLine[];
}
