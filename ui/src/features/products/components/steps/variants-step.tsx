import { FormRowCheckbox } from "@/components/form/form-row-checkbox";
import { Button } from "@/components/ui/button";
import { UseFormReturn } from "react-hook-form";
import { ProductFormData } from "../../hooks/use-product-form";
import { SelectVariants } from "../select-variants";
import { VariantForm } from "../variant-form";

type VariantsStepProps = {
  form: UseFormReturn<ProductFormData>;
  onNext: () => Promise<void>;
  onPrevious: () => void;
};

export const VariantsStep = ({ form, onNext, onPrevious }: VariantsStepProps) => {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <FormRowCheckbox name="hasVariants" label="This product has variants" />
        <VariantForm />
      </div>
      <SelectVariants
        hasVariants={form.watch("hasVariants")}
        formSetValue={(value) => form.setValue(
          "variants",
          value
        )}
        nextStep={(generateCombinations) => (
          <div className="flex justify-between pt-4 w-fit gap-4">
            <Button type="button" variant="outline" onClick={onPrevious}>
              Previous
            </Button>
            <Button
              type="button"
              onClick={async () => {
                const variants = await generateCombinations();
                if (form.getValues("hasVariants") && form.watch("variants") && form.watch("variants").length > 0) {
                  onNext();
                } else {
                  onNext();
                }
              } }
            >
              Next
            </Button>
          </div>
        )} productId={form.getValues("id") || 0} basePrice={form.getValues("basePrice") || "0"} baseStock={form.getValues("baseStock") || "0"}      />
    </div>
  );
}; 