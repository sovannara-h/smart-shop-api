import { FormRowSkeleton } from "@/components/form/form-row"
import { Button } from "@/components/ui/button"
import { FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { cn } from "@/lib/utils"
import { ReactElement } from "react"
import { useFormContext } from "react-hook-form"
import { useCategories } from "../hooks/use-categories"
import { CategoryForm } from "./category-form"


export type SelectCategoriesProps = {
  nextStep: ReactElement
}

export const SelectCategories = (props: SelectCategoriesProps) => {

  const {nextStep} = props;
  const { control } = useFormContext()
  const { data: categories, isLoading } = useCategories()

  if (isLoading) {
    return <FormRowSkeleton />
  }


  return (
    <FormField
      control={control}
      name="categories"
      render={({ field }) => (
        <FormItem>
          <div className="flex justify-between mb-4 items-center">
          <FormLabel className="font-semibold">
            Categories
          </FormLabel>
          <CategoryForm />
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {(categories || []).map((category) => (
              <Button
                key={category.id}
                type="button"
                variant="outline"
                className={cn(
                  "h-auto py-2 px-5",
                  field.value?.includes(category.id) && "border-primary bg-primary/10"
                )}
                onClick={() => {
                  const current = field.value || []
                  const value = current.includes(category.id)
                    ? current.filter(id => id !== category.id)
                    : [...current, category.id]
                  field.onChange(value)
                }}
              >
                {category.name}
              </Button>
            ))}
          </div>
          {nextStep}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}