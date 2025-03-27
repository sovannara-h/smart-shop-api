import { ProductResponse, ProductsParams, ProductsResponse } from "../types/product.types"

interface ProductCreateDTO {
  name: string
  description?: string
  categories: number[]
  hasVariants: boolean
  variants?: {
    sku: string
    attributeValues: Record<string, string>
    price: number
    stockQuantity: number
  }[]
}

type ProductImageCreateDTO = {
  id?: string;
  imageId: string;
  filename: string;
  sessions?: any;
}

export const productsApi = {
  getProducts: async (params: ProductsParams = {}): Promise<ProductsResponse> => {
    const searchParams = new URLSearchParams({
      page: String(params.page || 0),
      size: String(params.size || 10),
      ...(params.sort && { sort: params.sort }),
      ...(params.search && { search: params.search })
    })

    const response = await fetch(`http://localhost:8080/api/v1/products?${searchParams}`)
    if (!response.ok) throw new Error('Failed to fetch products')
    return response.json()
  },
  getProduct: async(id: string): Promise<ProductResponse> => {
    const response = await fetch(`http://localhost:8080/api/v1/products/${id}`);
    if(!response.ok) throw new Error('Failed to fetch product')
    return response.json();
  },
  createProduct: async (data: ProductCreateDTO, sessionId: string) => {
    const response = await fetch('http://localhost:8080/api/v1/products', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Session-Id': sessionId
      },
      body: JSON.stringify(data)
    })

    if (!response.ok) throw new Error('Erreur lors de la création du produit')
    return response.json()
  },
  upsertProductImages: async (productId: number | undefined, sessionId: string | null, data: ProductImageCreateDTO[]) => {
    if(!productId || !sessionId) return [];
    console.log(JSON.stringify({productImages: data}))
    const response = await fetch(`http://localhost:8080/api/v1/products/${productId}/images`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Session-Id': sessionId
      },
      body: JSON.stringify({productImages: data})
    });
    if (!response.ok) throw new Error('Erreur lors de l\;upsert des images du produit');
    return response.json()
  },
  confirmProduct: async (sessionId: string) => {
    if(!sessionId) return;
    const response = await fetch(`http://localhost:8080/api/v1/sessions/${sessionId}/confirm`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    })
    if (!response.ok) throw new Error('Erreur lors de la confirmation du produit');
    return response.json()
  },
  cancelProduct: async (sessionId: string) => {
    if(!sessionId) return;
    const response = await fetch(`http://localhost:8080/api/v1/sessions/${sessionId}/cancel`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    })
    if (!response.ok) throw new Error('Erreur lors de la confirmation du produit');
    return response.json()
  },
}