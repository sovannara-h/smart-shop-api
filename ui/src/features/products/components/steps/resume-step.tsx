import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import { useFormContext } from "react-hook-form";
import { ProductFormData } from "../../hooks/use-product-form";

export type ResumeStepProps = {
  navigation?: React.ReactNode;
};

export const ResumeStep = ({ navigation }: ResumeStepProps) => {
  const { getValues } = useFormContext<ProductFormData>();
  const formData = getValues();

  console.log("FORM DATA",formData)
  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold">Résumé du produit</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="p-6 space-y-4">
          <h3 className="font-semibold text-lg">Informations générales</h3>
          <div className="space-y-2">
            <p><span className="font-medium">Nom:</span> {formData.name}</p>
            <p><span className="font-medium">Description:</span> {formData.description}</p>
            <div className="flex gap-2 flex-wrap">
              <span className="font-medium">Catégories:</span>
              {formData.categories?.map((cat) => (
                <Badge key={cat.id} variant="outline">{cat.name}</Badge>
              ))}
            </div>
          </div>
        </Card>

        <Card className="p-6 space-y-4">
          <h3 className="font-semibold text-lg">Variantes</h3>
          <ScrollArea className="h-[200px] pr-4">
            <div className="space-y-3">
              {formData.variants?.map((variant, idx) => (
                <div key={variant.sku || idx} className="p-3 border rounded-lg">
                  <p><span className="font-medium">SKU:</span> {variant.sku}</p>
                  <p><span className="font-medium">Prix:</span> {variant.price}€</p>
                  <p><span className="font-medium">Stock:</span> {variant.stockQuantity}</p>
                  <div className="flex gap-2 flex-wrap mt-2">
                    {variant.attributeValues.map((av) => (
                      <Badge key={av.id} variant={"secondary"}>
                        {av.name}: {av.value}
                      </Badge>
                    ))}
                    {/* {Object.entries(variant.attributeValues || {}).map(([key, value]) => (
                      <Badge key={`${variant.sku}-${key}`} variant="secondary">
                        {key}: {value}
                      </Badge>
                    ))} */}
                  </div>
                </div>
              ))}
            </div>
          </ScrollArea>
        </Card>
      </div>

      {navigation}
    </div>
  );
};