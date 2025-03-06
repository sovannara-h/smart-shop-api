import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Plus, X } from "lucide-react";
import { useState } from "react";

interface VariantBuilderProps {
  onAttributesChange: (attributes: Array<{name: string, values: string[]}>) => void;
  onPreviewVariants: () => void;
}

export function VariantBuilder({ onAttributesChange, onPreviewVariants }: VariantBuilderProps) {
  const [attributes, setAttributes] = useState<Array<{name: string, values: string[]}>>([]);
  const [newValue, setNewValue] = useState<string>("");

  const addAttribute = () => {
    setAttributes([...attributes, { name: '', values: [] }]);
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

  return (
    <div className="space-y-4">
      {attributes.map((attribute, index) => (
        <div key={index} className="p-4 border rounded-lg space-y-2">
          <div className="flex items-center gap-2">
            <Input
              placeholder="Nom de l'attribut (ex: Couleur)"
              value={attribute.name}
              onChange={(e) => {
                const newAttributes = [...attributes];
                newAttributes[index].name = e.target.value;
                setAttributes(newAttributes);
                onAttributesChange(newAttributes);
              }}
            />
            <Button
              variant="destructive"
              size="icon"
              onClick={() => removeAttribute(index)}
            >
              <X className="h-4 w-4" />
            </Button>
          </div>

          <div className="flex items-center gap-2">
            <Input
              placeholder="Nouvelle valeur"
              value={newValue}
              onChange={(e) => setNewValue(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  addValueToAttribute(index);
                }
              }}
            />
            <Button onClick={() => addValueToAttribute(index)}>
              Ajouter
            </Button>
          </div>

          <div className="flex flex-wrap gap-2 mt-2">
            {attribute.values.map((value, valueIndex) => (
              <div key={valueIndex} className="bg-secondary px-2 py-1 rounded-md flex items-center gap-1">
                <span>{value}</span>
                <button
                  onClick={() => {
                    const newAttributes = [...attributes];
                    newAttributes[index].values = newAttributes[index].values.filter((_, i) => i !== valueIndex);
                    setAttributes(newAttributes);
                    onAttributesChange(newAttributes);
                  }}
                  className="text-muted-foreground hover:text-foreground"
                >
                  ×
                </button>
              </div>
            ))}
          </div>
        </div>
      ))}

      <div className="flex gap-4">
        <Button onClick={addAttribute}>
          <Plus className="mr-2 h-4 w-4" />
          Ajouter un attribut
        </Button>
        <Button onClick={onPreviewVariants}>
          Prévisualiser les variantes
        </Button>
      </div>
    </div>
  );
} 