import { SelectCategories } from "@/features/categories/components/select-categories";
import { UseFormReturn } from "react-hook-form";
import { ProductFormData } from "../../hooks/use-product-form";

type CategoriesStepProps = {
  form: UseFormReturn<ProductFormData>;
  navigation: React.ReactNode;
};

export const CategoriesStep = ({ navigation }: CategoriesStepProps) => {
  return (
    <div className="space-y-4">
      <SelectCategories nextStep={navigation} />
    </div>
  );
}; 