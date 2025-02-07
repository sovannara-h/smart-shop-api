import { OrderItem } from "./order-item.entity";

enum Status {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
  }

export type Order = {
    id: number;
    orderDate: Date
    status: Status;
    totalAmount: number;
    items: OrderItem[];
    version: number;
    createdAt: Date;
    updatedAt: Date;
  }
  