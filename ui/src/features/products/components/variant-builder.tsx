import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Plus, X } from "lucide-react";
import { useState } from "react";
import { variantsApi } from "../api/variants.api";

interface Attribute {
  name: string;
  values: string[];
}

interface VariantBuilderProps {
  onAttributesChange: (attributes: Attribute[]) => void;
  generateVariants: () => void;
}

export function VariantBuilder({
  onAttributesChange,
  generateVariants,
}: VariantBuilderProps) {
  const [attributes, setAttributes] = useState<Attribute[]>([]);
  const [newValue, setNewValue] = useState("");

  const addAttribute = () => {
    setAttributes([...attributes, { name: "", values: [] }]);
  };

  const removeAttribute = (index: number) => {
    const newAttributes = attributes.filter((_, i) => i !== index);
    setAttributes(newAttributes);
    onAttributesChange(newAttributes);
  };

  const addValueToAttribute = (attributeIndex: number) => {
    if (!newValue.trim()) return;

    const newAttributes = [...attributes];
    newAttributes[attributeIndex].values.push(newValue.trim());
    setAttributes(newAttributes);
    setNewValue("");
    onAttributesChange(newAttributes);
  };

  const removeValue = (attributeIndex: number, valueIndex: number) => {
    const newAttributes = [...attributes];
    newAttributes[attributeIndex].values.splice(valueIndex, 1);
    setAttributes(newAttributes);
    onAttributesChange(newAttributes);
  };

  return (
    <div className="space-y-6">
      {attributes.map((attribute, attrIndex) => (
        <div key={attrIndex} className="p-4 border rounded-lg space-y-4">
          <div className="flex items-center gap-2">
            <Input
              placeholder="Nom de l'attribut (ex: Couleur)"
              value={attribute.name}
              onChange={(e) => {
                const newAttributes = [...attributes];
                newAttributes[attrIndex].name = e.target.value;
                setAttributes(newAttributes);
                onAttributesChange(newAttributes);
              }}
            />
            <Button
              variant="destructive"
              size="icon"
              onClick={() => removeAttribute(attrIndex)}
            >
              <X className="h-4 w-4" />
            </Button>
          </div>

          <div className="flex flex-wrap gap-2">
            {attribute.values.map((value, valueIndex) => (
              <span
                key={valueIndex}
                className="px-3 py-1 bg-muted rounded-full flex items-center gap-2"
              >
                {value}
                <button
                  onClick={() => removeValue(attrIndex, valueIndex)}
                  className="text-muted-foreground hover:text-destructive"
                >
                  <X className="h-3 w-3" />
                </button>
              </span>
            ))}
          </div>

          <div className="flex items-center gap-2">
            <Input
              placeholder="Nouvelle valeur"
              value={newValue}
              onChange={(e) => setNewValue(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === "Enter") {
                  addValueToAttribute(attrIndex);
                }
              }}
            />
            <Button onClick={() => addValueToAttribute(attrIndex)}>
              Ajouter
            </Button>
          </div>
        </div>
      ))}

      <div className="flex justify-between">
        <Button type="button" variant="outline" onClick={addAttribute}>
          <Plus className="mr-2 h-4 w-4" />
          Ajouter un attribut
        </Button>

        <Button
          type="button"
          onClick={generateVariants}
          disabled={!attributes.every((a) => a.name && a.values.length > 0)}
        >
          Prévisualiser les variantes
        </Button>
      </div>
    </div>
  );
}
