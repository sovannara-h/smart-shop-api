import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Dispatch, SetStateAction } from "react";

export type  VariantsRowProps = {
  attribute: any
  setAttributes: Dispatch<SetStateAction<number[]>>
  selectedAttributes: number[]
}

export const VariantsRow = (props: VariantsRowProps) => {

    const {attribute, selectedAttributes, setAttributes} = props;


    return (

        <div

      data-cy={"product-form-variant"}
      className={
        "VariantRow space-y-2 flex flex-row items-center justify-between rounded-lg border p-4"
      }
    >
      <div className="space-y-0.5">
        <Label className="text-base font-medium capitalize">
          {attribute.name}
        </Label>
        <p
          color={"muted"}
          className="text-[0.8rem] capitalize"
        >
          {attribute.values.map((o) => o).join(", ")}
        </p>
      </div>
      <div className="flex items-center gap-4">

      <Switch 
      checked={selectedAttributes.includes(attribute.id)}
      onCheckedChange={() => {
        setAttributes(prev => {
          if(prev.includes(attribute.id)) {
            return prev.filter(a => a !== attribute.id)
          }
          return [...prev, attribute.id]
        })
      }}/>
        {/* <Switch
          data-cy={"product-form-variant-switch"}
          data-value={variant.id}
          onCheckedChange={() => {
            const indexOfProductVariant = productVariants.findIndex(
              (pv) => pv.variant_id === variant.id,
            );
            if (indexOfProductVariant >= 0) {
              removeProductVariant(indexOfProductVariant);
            } else {
              appendProductVariant({
                variant_id: variant.id,
              });
            }
          }}
          checked={!!productVariants.find((pv) => pv.variant_id === variant.id)}
        /> */}
      </div>
    </div>

    );
}