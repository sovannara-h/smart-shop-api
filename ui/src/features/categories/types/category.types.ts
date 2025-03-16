export type Category = {
    id: number
    name: string
    active: boolean
    createdAt: string
    updatedAt: string
}

export type CategoriesResponse = {
    data: {
        content: Category[]
        totalPages: number
      totalElements: number
      size: number
      number: number
    }
}

export interface CategoriesParams {
    page?: number
    size?: number
    sort?: string
    search?: string
  }
