import { FormRowInput } from "@/components/form/form-row-input";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Form, FormLabel } from "@/components/ui/form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import { CirclePlus } from "lucide-react";
import { useState } from "react";
import { useFieldArray, useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

export type VariantFormProps = {

}

export const VariantFormSchema = z.object({
  name: z.string().min(2, "Le nom doit contenir au moins 2 caractères"),
  attribute_values: z.array(z.object({
    value: z.string().min(1, "La valeur est requise")
  })).min(1, "Au moins une valeur est requise")
})

export type VariantFormData = z.infer<typeof VariantFormSchema>

export const VariantForm = (props: VariantFormProps) => {

    const {} = props;

    const queryClient = useQueryClient()
    const [open, setOpen] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const form = useForm<VariantFormData>({
      resolver: zodResolver(VariantFormSchema)
    })
    const {control, formState: { errors }} = form;

    const {fields, append, remove} = useFieldArray({
        control,
        name: "attribute_values"
    })

    const onSubmit = async (data: VariantFormData) => {
      setIsSubmitting(true);
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
              const errorData = await response.json();
              throw new Error(errorData.message || "Erreur lors de la création de l'attribut");
          }

          toast.success("Attribut créé avec succès");
          queryClient.invalidateQueries({queryKey: ["attributes-with-categories"]})
          setOpen(false);
          return await response.json();
      } catch (error) {
          if (error instanceof z.ZodError) {
              error.errors.forEach(err => {
                  toast.error(err.message);
              });
          } else {
              toast.error(error instanceof Error ? error.message : "Une erreur inattendue s'est produite");
          }
          throw error;
      } finally {
          setIsSubmitting(false);
      }
    }

    return (
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
            <Button type="button" variant={"outline"}>
              <CirclePlus />
              Add variant</Button>
        </DialogTrigger>
        <DialogContent>
        <DialogHeader>
          <DialogTitle>Add New Variant Attribute</DialogTitle>
          <DialogDescription>
            Create a new variant attribute with multiple values that can be used to define product variations.
          </DialogDescription>
        </DialogHeader>
            <Form {...form}>
                <form className="space-y-6" onSubmit={form.handleSubmit(onSubmit)} onClick={(e) => e.stopPropagation()}>
                    <FormRowInput label={"Name"} name={"name"} />
                    <div className="space-y-4">
                        <FormLabel className="font-semibold">Values</FormLabel>
                    {fields.map((field, index) => {
                        return <div key={field.id} className="flex space-x-2">
                            <FormRowInput name={`attribute_values.${index}.value`} label={""} />
                            <Button 
                              type="button" 
                              variant="destructive" 
                              onClick={() => remove(index)}
                              disabled={isSubmitting}
                            >
                              x
                            </Button>
                            </div>
                    })}
                    </div>
                    <Button 
                      type="button" 
                      onClick={() => append({value: ""})}
                      disabled={isSubmitting}
                    >
                      Add value
                    </Button>
                    <Button 
                      type="submit"
                      disabled={isSubmitting}
                    >
                      {isSubmitting ? "Submitting..." : "Create variant"}
                    </Button>
                </form>
            </Form>
        </DialogContent>
      </Dialog>
    );
}