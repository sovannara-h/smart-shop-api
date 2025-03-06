import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { FormControl, FormField, FormItem, FormLabel } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { VariantBuilder } from "@/components/variant-builder";
import { VariantPreview } from "@/components/variant-preview";
import { useFetchDataWithPageable } from "@/hooks/useFetchDataWithPageable";
import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft } from "lucide-react";
import { useState } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { useNavigate, useParams } from 'react-router-dom';
import * as z from "zod";

const productSchema = z.object({
  name: z.string()
    .min(2, "Le nom doit contenir au moins 2 caractères")
    .max(100, "Le nom ne peut pas dépasser 100 caractères"),
  description: z.string().max(1000, "La description ne peut pas dépasser 1000 caractères").optional(),
  active: z.boolean().default(true),
  categories: z.array(z.number()).min(1, "Sélectionnez au moins une catégorie"),
  hasVariants: z.boolean().default(false),
  basePrice: z.number().min(0, "Le prix ne peut pas être négatif").optional(),
  baseStock: z.number().min(0, "Le stock ne peut pas être négatif").optional(),
  interactions: z.record(z.string(), z.any()).optional(),
});

type ProductFormData = z.infer<typeof productSchema>;

export function ProductForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditing = Boolean(id);
  
  const [showVariantPreview, setShowVariantPreview] = useState(false);
  const [attributes, setAttributes] = useState<Array<{name: string, values: string[]}>>([]);
  const [variantPreviews, setVariantPreviews] = useState<any[]>([]);

  const { queryRes: {data: categoriesData, isLoading: loadingData}} = useFetchDataWithPageable({
    table: "categories"
  });

  const categories = categoriesData?.data.content || [];

  const defaultValues: ProductFormData = {
    name: "",
    description: "",
    active: true,
    categories: [],
    hasVariants: false,
    basePrice: 0,
    baseStock: 0,
    interactions: {},
  };

  const form = useForm<ProductFormData>({
    resolver: zodResolver(productSchema),
    defaultValues
  });

  const handleAttributesChange = (newAttributes: Array<{name: string, values: string[]}>) => {
    setAttributes(newAttributes);
  };

  const handlePreviewVariants = async (form: any) => {
    if (!attributes.length) return;

    try {
      const response = await fetch(`http://localhost:8080/api/products/${id}/variants/preview`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          attributes: attributes.reduce((acc, attr) => ({
            ...acc,
            [attr.name]: attr.values
          }), {}),
          basePrice: form.getValues('basePrice'),
          baseStock: form.getValues('baseStock')
        })
      });

      if (response.ok) {
        const data = await response.json();
        setVariantPreviews(data.data);
        setShowVariantPreview(true);
      }
    } catch (error) {
      console.error('Erreur lors de la prévisualisation des variantes:', error);
    }
  };

  const handleVariantConfirm = async (variants: any[]) => {
    try {
      const response = await fetch(`http://localhost:8080/api/products/${id}/variants/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          productId: id,
          variants: variants
        })
      });

      if (response.ok) {
        setShowVariantPreview(false);
        // Rafraîchir les données du produit ou rediriger
        navigate('/products');
      }
    } catch (error) {
      console.error('Erreur lors de la création des variantes:', error);
    }
  };

  const handleSubmit = async (data: ProductFormData) => {
    try {
      console.log(data)
      const response = await fetch('http://localhost:8080/api/products', {
        method: isEditing ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });

      if (response.ok) {
        const result = await response.json();
        if (data.hasVariants) {
          handlePreviewVariants(result.data.id);
        } else {
          navigate('/products');
        }
      }
    } catch (error) {
      console.error('Erreur lors de la sauvegarde du produit:', error);
    }
  };

  if (loadingData) {
    return <div>Chargement...</div>;
  }

  return (
    <>
      <div className="flex items-center gap-4 mb-8">
        <Button
          variant="outline"
          size="icon"
          onClick={() => navigate('/products')}
        >
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <h1 className="text-3xl font-bold tracking-tight">
          {isEditing ? 'Modifier le produit' : 'Nouveau produit'}
        </h1>
      </div>

      <div className="max-w-2xl">
        <FormProvider {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
            <div className="space-y-4">
              <FormField
                control={form.control}
                name="name"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nom du produit</FormLabel>
                    <FormControl>
                      <Input placeholder="Entrez le nom du produit" {...field} />
                    </FormControl>
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Description</FormLabel>
                    <FormControl>
                      <Textarea 
                        placeholder="Entrez la description du produit"
                        className="min-h-[100px]"
                        {...field}
                      />
                    </FormControl>
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="hasVariants"
                render={({ field }) => (
                  <FormItem className="flex flex-row items-center space-x-2">
                    <FormControl>
                      <Checkbox
                        checked={field.value}
                        onCheckedChange={field.onChange}
                      />
                    </FormControl>
                    <FormLabel className="font-normal">
                      Ce produit a des variantes
                    </FormLabel>
                  </FormItem>
                )}
              />

              {form.watch("hasVariants") ? (
                <div className="space-y-4">
                  <div className="grid gap-4 sm:grid-cols-2">
                    <FormField
                      control={form.control}
                      name="basePrice"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Prix de base</FormLabel>
                          <FormControl>
                            <Input
                              type="number"
                              min="0"
                              step="0.01"
                              {...field}
                              onChange={(e) => field.onChange(Number(e.target.value))}
                            />
                          </FormControl>
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="baseStock"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Stock de base</FormLabel>
                          <FormControl>
                            <Input
                              type="number"
                              min="0"
                              {...field}
                              onChange={(e) => field.onChange(Number(e.target.value))}
                            />
                          </FormControl>
                        </FormItem>
                      )}
                    />
                  </div>
                  
                  <VariantBuilder
                    onAttributesChange={handleAttributesChange}
                    onPreviewVariants={() => handlePreviewVariants(form)}
                  />
                </div>
              ) : null}

              <FormField
                control={form.control}
                name="categories"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Catégories</FormLabel>
                    <Select
                      value={field.value.length > 0 ? String(field.value[field.value.length - 1]) : undefined}
                      onValueChange={(value) => {
                        const numValue = Number(value);
                        if (!field.value.includes(numValue)) {
                          field.onChange([...field.value, numValue]);
                        }
                      }}
                    >
                      <SelectTrigger>
                        <SelectValue>
                          {field.value.length > 0 
                            ? `${field.value.length} catégorie(s) sélectionnée(s)` 
                            : "Sélectionnez les catégories"}
                        </SelectValue>
                      </SelectTrigger>
                      <SelectContent>
                        {categories.map(({id, name}) => (
                          <SelectItem key={id} value={String(id)}>
                            {name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <div className="flex flex-wrap gap-2 mt-2">
                      {field.value.map((catId) => {
                        const category = categories.find(c => c.id === catId);
                        return category ? (
                          <div key={catId} className="flex items-center gap-1 bg-secondary px-2 py-1 rounded-md">
                            <span>{category.name}</span>
                            <button
                              type="button"
                              onClick={() =>  field.onChange(field.value.filter(id => id !== catId))}
                              className="text-muted-foreground hover:text-foreground"
                            >
                              ×
                            </button>
                          </div>
                        ) : null;
                      })}
                    </div>
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
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
                      Produit actif
                    </FormLabel>
                  </FormItem>
                )}
              />
            </div>

            <div className="flex gap-4">
              <Button type="submit">
                {isEditing ? "Mettre à jour" : "Créer"}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate('/products')}
              >
                Annuler
              </Button>
            </div>
          </form>
        </FormProvider>
      </div>

      {showVariantPreview && (
        <VariantPreview
          variants={variantPreviews}
          onClose={() => setShowVariantPreview(false)}
          onConfirm={handleVariantConfirm}
        />
      )}
    </>
  );
}