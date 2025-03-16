import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { UseFormReturn } from "react-hook-form";
import { ProductFormData } from "../hooks/use-product-form";


export type  ProductVariantCombinationsProps = {
    combinations: {[key: string]: string}[]
    form: UseFormReturn<ProductFormData>
}

export const ProductVariantCombinations = (props: ProductVariantCombinationsProps) => {

    const {combinations, form} = props;
    console.log(combinations)

    if(!combinations || !combinations?.length) return null;
    return (
      <div className={`ProductVariantCombinations`}>
        <Table>
              <TableHeader>
                <TableRow>
                  {Object.keys(combinations.at(0) || {}).map((key) => (
                    <TableHead key={key}>{key}</TableHead>
                  ))}
                </TableRow>
              </TableHeader>

              <TableBody>
                {combinations.map((combination, index) => {
                  return (
                    <TableRow key={index}>
                      {Object.entries(combination).map(([key, value]) => {
                        if (key !== "price" && key !== "stockQuantity")
                          return <TableCell>{value}</TableCell>;
                        return (
                          <TableCell>
                            <Input
                            className="max-w-[6rem]"
                              value={value}
                              onChange={(e) => {
                                form.setValue(
                                  `variants.${index}.${key}`,
                                  e.target.value,
                                );
                              }}
                            />
                          </TableCell>
                        );
                      })}
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
      </div>
    );
}