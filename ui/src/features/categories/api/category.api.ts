import { CategoriesParams, CategoriesResponse } from "../types/category.types"


type CategoryCreateDTO = {
    name: string
    active: boolean
  }

  
export const categoryApi = {
    getCategories: async (params: CategoriesParams = {}): Promise<CategoriesResponse> => {
        const searchParams = new URLSearchParams({
            page: String(params.page || 0),
            size: String(params.size || 10),
            ...(params.sort && { sort: params.sort }),
            ...(params.search && { search: params.search })
          })
      
          const response = await fetch(`http://localhost:8080/api/v1/categories?${searchParams}`)
          if (!response.ok) throw new Error('Failed to fetch categories')
          return response.json()
    },
    createCategory: async (data: CategoryCreateDTO) => {
        const response = await fetch('http://localhost:8080/api/v1/categories', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(data)
        })
        
        if (!response.ok) throw new Error('Erreur lors de la création de la catégorie')
        return response.json()
      }
}