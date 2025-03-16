import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useState } from "react";
import { AttributeValue, Variant } from "../types/variant.types";

interface VariantPreviewProps {
  variants: Variant[];
  onClose: () => void;
  onConfirm: (variants: Variant[]) => void;
}

export function VariantPreview({
  variants,
  onClose,
  onConfirm,
}: VariantPreviewProps) {
  const [editedVariants, setEditedVariants] = useState(variants);

  return (
    <Dialog open onOpenChange={onClose}>
      <DialogContent className="">
        <DialogHeader>
          <DialogTitle>Prévisualisation des variantes</DialogTitle>
        </DialogHeader>

        <div className="max-h-[600px] overflow-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>SKU</TableHead>
                {Object.keys(variants[0]?.attributeValues || {}).map((attr) => (
                  <TableHead key={attr}>{attr}</TableHead>
                ))}
                <TableHead>Prix</TableHead>
                <TableHead>Stock</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {editedVariants.map((variant, index) => (
                <TableRow key={variant.sku}>
                  <TableCell>{variant.sku}</TableCell>
                  {Object.values(variant.attributeValues).map(
                    (value: AttributeValue, i) => (
                      <TableCell key={i}>{value.value}</TableCell>
                    ),
                  )}
                  <TableCell>
                    <Input
                      className="w-3xs"
                      type="number"
                      min="0"
                      step="0.01"
                      value={variant.price}
                      onChange={(e) => {
                        const newVariants = [...editedVariants];
                        newVariants[index].price = Number(e.target.value);
                        setEditedVariants(newVariants);
                      }}
                    />
                  </TableCell>
                  <TableCell>
                    <Input
                      className="w-3xs"
                      type="number"
                      min="0"
                      value={variant.stockQuantity}
                      onChange={(e) => {
                        const newVariants = [...editedVariants];
                        newVariants[index].stockQuantity = Number(
                          e.target.value,
                        );
                        setEditedVariants(newVariants);
                      }}
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>

        <div className="flex justify-end gap-4">
          <Button variant="outline" onClick={onClose}>
            Annuler
          </Button>
          <Button onClick={() => onConfirm(editedVariants)}>Confirmer</Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
