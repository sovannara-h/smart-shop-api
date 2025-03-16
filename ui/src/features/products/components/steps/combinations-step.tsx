
import { ReactNode } from "react";
import { UseFormReturn } from "react-hook-form";
import { ProductFormData } from "../../hooks/use-product-form";
import { ProductVariantCombinations } from "../product-variant-combinations";

type CombinationsStepProps = {
  form: UseFormReturn<ProductFormData>;
  navigation: ReactNode
};

export const CombinationsStep = ({ form, navigation }: CombinationsStepProps) => {
  return (
    <div className="space-y-4">
      <ProductVariantCombinations combinations={form.getValues("combinations")} form={form} />
      {navigation}
    </div>
  );
}; 