import { FormRowInput } from "@/components/form/form-row-input";
import { FormRowInputDecimal } from "@/components/form/form-row-input-decimal";
import { FormRowTextarea } from "@/components/form/form-row-textarea";
import { UseFormReturn } from "react-hook-form";
import { ProductFormData } from "../../hooks/use-product-form";

type GeneralStepProps = {
  form: UseFormReturn<ProductFormData>;
  navigation: React.ReactNode;
};

export const GeneralStep = ({ navigation }: GeneralStepProps) => {
  return (
    <div className="space-y-4">
      <FormRowInput label="Name" name="name" required placeholder="Enter product name" />
      <FormRowTextarea label="Description" name="description" placeholder="Enter product description" />
      <div className="flex gap-8">
        <FormRowInputDecimal
          label="Base Price"
          name="basePrice"
          type="number"
          required
          placeholder="Enter base price"
        />
        <FormRowInputDecimal
          label="Initial Stock"
          name="baseStock"
          type="number"
          required
          placeholder="Enter initial stock quantity"
        />
      </div>
      {navigation}
    </div>
  );
}; 