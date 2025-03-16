import { useFormContext } from "react-hook-form"
import { z } from "zod"

export const VariantFormSchema = z.object({
  name: z.string().min(2, "Le nom doit contenir au moins 2 caractères"),
  attribute_values: z.array(z.object({
    value: z.string().min(1, "La valeur est requise")
  })).min(1, "Au moins une valeur est requise")
})

export type VariantFormData = z.infer<typeof VariantFormSchema>

export const useVariantForm = () => {
    const form = useFormContext<VariantFormData>();

    const onSubmit = async (data: VariantFormData) => {

        try {
            const validatedData = await VariantFormSchema.parseAsync(data);
            const response = await fetch('http://localhost:8080/api/v1/attributes/with-values', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    name: validatedData.name,
                    values: validatedData.attribute_values.map(av => av.value)
                })
            });

            if (!response.ok) {
                throw new Error("Erreur lors de la création de l'attribut");
            }
            return await response.json();
        } catch (error) {
            console.error("Erreur:", error);
            throw error;
        }
    }

    return {
        form,
        onSubmit
    }
}