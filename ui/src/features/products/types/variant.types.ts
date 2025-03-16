export interface AttributeValue {
  id: number;
  value: string;
  createdAt: string;
  updatedAt: string;
}

export interface Variant {
  id: number;
  sku: string;
  price: number;
  stockQuantity: number;
  attributeValues: AttributeValue[];
}

export interface GenerateVariantsDTO {
  attributes: {
    name: string;
    values: string[];
  }[];
  basePrice: number;
  baseStock: number;
}

export interface VariantResponse {
  success: boolean;
  data: Variant[];
  message: string;
  error: string | null;
  timestamp: string;
}
