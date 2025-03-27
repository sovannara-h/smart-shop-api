import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { UseFormReturn } from "react-hook-form";
import { ProductFormData, ProductVariantData } from "../hooks/use-product-form";

export type ProductVariantCombinationsProps = {
    variants: ProductVariantData[] | undefined
    form: UseFormReturn<ProductFormData>
}

export const ProductVariantCombinations = (props: ProductVariantCombinationsProps) => {
    const { variants, form } = props;

    if (!variants || !variants.length) return null;

    const attributeNames = variants[0]?.attributeValues.map(av => av.name) || [];

    return (
        <div className="ProductVariantCombinations">
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead>SKU</TableHead>
                        {attributeNames.map(name => (
                            <TableHead key={name}>{name}</TableHead>
                        ))}
                        <TableHead>Prix</TableHead>
                        <TableHead>Stock</TableHead>
                    </TableRow>
                </TableHeader>

                <TableBody>
                    {variants.map((variant, index) => (
                        <TableRow key={variant.sku}>
                            <TableCell>{variant.sku}</TableCell>
                            {variant.attributeValues.map(av => (
                                <TableCell key={av.id}>{av.value}</TableCell>
                            ))}
                            <TableCell>
                                <Input
                                    className="max-w-[6rem]"
                                    type="number"
                                    value={variant.price}
                                    onChange={(e) => {
                                        form.setValue(`variants.${index}.price`, Number(e.target.value));
                                    }}
                                />
                            </TableCell>
                            <TableCell>
                                <Input
                                    className="max-w-[6rem]"
                                    type="number"
                                    value={variant.stockQuantity}
                                    onChange={(e) => {
                                        form.setValue(`variants.${index}.stockQuantity`, Number(e.target.value));
                                    }}
                                />
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
}