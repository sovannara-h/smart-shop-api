
import { ReactNode, useState } from "react";
import { useSelectVariants } from "../hooks/use-select-variants";
import { VariantsRow } from "./variant-row";



type SelectVariantsProps = {
  hasVariants: boolean;
  productId: number,
  basePrice: string | number,
  baseStock: string | number,
  formSetValue: (value: any) => any;
  nextStep: (func: () => Promise<void>) => ReactNode;
};

export const SelectVariants = (props: SelectVariantsProps) => {
  const { hasVariants, formSetValue,nextStep, basePrice, baseStock, productId } = props;
  const [selectedAttributes, setSelectedAttributes] = useState<number[]>([]);

  const {attributes, generateCombinations, combinations} = useSelectVariants(formSetValue);

  if (!hasVariants) return null;

  return <div className={`SelectVariants space-y-2`}>
    
    {attributes.map((attribute) => {
      return <VariantsRow key={attribute.id} attribute={attribute} selectedAttributes={selectedAttributes} setAttributes={setSelectedAttributes}/>
    })}
    {nextStep(() => generateCombinations({productId, basePrice , baseStock  ,  attributes : selectedAttributes}))}
  </div>;
};
