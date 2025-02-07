import { Product } from "./product.entity";

export type Category = {
    id: string;
    name: string;
    active: boolean;
    createdAt: Date;
    updatedAt: Date;
    products?: Product[]
}