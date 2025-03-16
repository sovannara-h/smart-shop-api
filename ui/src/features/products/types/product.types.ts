export interface Product {
    id: number
    name: string
    price: number
    stockInQuantity: number
    description?: string
    active: boolean
  }
  
  export interface ProductsResponse {
    data: {
      content: Product[]
      totalPages: number
      totalElements: number
      size: number
      number: number
    }
  }
  

  export interface ProductResponse {
    data: {
      content: Product
    }
  }

  export interface ProductsParams {
    page?: number
    size?: number
    sort?: string
    search?: string
  }

