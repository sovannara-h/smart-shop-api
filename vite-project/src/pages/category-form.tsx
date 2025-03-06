import * as z from "zod";
import { Button } from "../components/ui/button";
import { Checkbox } from "../components/ui/checkbox";
import { Form, FormControl, FormField, FormItem, FormLabel } from "../components/ui/form";
import { Input } from "../components/ui/input";

const categorySchema = z.object({
  name: z.string().min(1, "Le nom est requis"),
  active: z.boolean().default(true),
});

type CategoryFormData = z.infer<typeof categorySchema>;

export type CategoryFormProps = {
  onSubmit: (data: CategoryFormData) => void;
  initialData?: CategoryFormData;
  isEditing?: boolean;
};

export const CategoryForm = (props: CategoryFormProps) => {
  const { onSubmit, initialData, isEditing = false } = props;

  const defaultValues: CategoryFormData = {
    name: initialData?.name || "",
    active: initialData?.active ?? true,
  };

  const handleSubmit = async (data: CategoryFormData) => {
    try {
      const response = await fetch('http://localhost:8080/api/categories', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: data.name,
          active: data.active,
        }),
      });

      if (!response.ok) {
        throw new Error('Erreur lors de la création de la catégorie');
      }

      const result = await response.json();
      console.log('Catégorie créée avec succès:', result);
    } catch (error) {
      console.error('Erreur:', error);
    }
  };

  return (
    <Form
      zodSchema={categorySchema}
      initialData={defaultValues}
      handleSubmit={handleSubmit}
      className="space-y-6"
    >
      <div className="space-y-4">
        <FormField
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Nom de la catégorie</FormLabel>
              <FormControl>
                <Input 
                  placeholder="Entrez le nom de la catégorie" 
                  {...field} 
                />
              </FormControl>
            </FormItem>
          )}
        />

        <FormField
          name="active"
          render={({ field }) => (
            <FormItem className="flex flex-row items-center space-x-2">
              <FormControl>
                <Checkbox
                  checked={field.value}
                  onCheckedChange={field.onChange}
                />
              </FormControl>
              <FormLabel className="font-normal">
                Catégorie active
              </FormLabel>
            </FormItem>
          )}
        />
      </div>

      <div className="flex justify-end">
        <Button type="submit">
          {isEditing ? "Mettre à jour" : "Créer"}
        </Button>
      </div>
    </Form>
  );
};