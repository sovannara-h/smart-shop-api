import { useQuery } from '@tanstack/react-query'
import { categoryApi } from '../api/category.api'

export const useCategories = () => {
  return useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryApi.getCategories({ size: 100 }), // On veut toutes les catégories
    select: (data) => data.data.content,

  })
} 