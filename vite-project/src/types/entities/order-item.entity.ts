import { Product } from "./product.entity";

export type OrderItem = {
    id: number;
    product: Product;
    quantity: number;
    unitPrice: number;
  }