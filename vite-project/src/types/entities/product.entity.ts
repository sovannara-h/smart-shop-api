import { Category } from "./category.entity";

export type Product = {
    id: number;
    categories: Category[]; 
    name: string;
    description: string | null;
    price: number;
    rating: number;
    numberOfReviews: number;
    active: boolean;
    attributes: Record<string, any>;
    interactions: Record<string, any>;
    version: number;
    stockInQuantity: number;
    createdAt: Date;
    updatedAt: Date
}