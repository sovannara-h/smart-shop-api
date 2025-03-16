import { zodResolver } from '@hookform/resolvers/zod'
import { useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'
import { categoryApi } from '../api/category.api'
const categorySchema = z.object({
    name: z.string().min(2, "Name must contain at least 2 characters"),
    active: z.boolean().default(true)
  })
  
  type CategoryFormData = z.infer<typeof categorySchema>

  
export const useCategoryForm = () => {
  const queryClient = useQueryClient();
     const [open, setOpen] = useState(false);
    const form = useForm<CategoryFormData>({
        resolver: zodResolver(categorySchema),
        defaultValues: {
            active: true
        }
    })

    const onSubmit = async (data: CategoryFormData) => {
        try {
          const res = await categoryApi.createCategory(data)
          toast.success("Category created successfully");
          queryClient.invalidateQueries({ queryKey: ['categories'] });
          form.reset();
          setOpen(false);
        } catch (error) {
          toast.error(error instanceof Error ? error.message : "An error occurred during creation");
        }
      }

    return {
        open, 
        setOpen,

        form,
        onSubmit
    }
}