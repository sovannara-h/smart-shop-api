import { Button } from "@/components/ui/button";
import { DataTable } from "@/components/ui/data-table";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useFetchDataWithPageable } from "@/hooks/useFetchDataWithPageable";
import { Product } from "@/types/entities/product.entity";
import { ColumnDef } from "@tanstack/react-table";
import { Plus, Search } from "lucide-react";
import { useNavigate } from "react-router-dom";



const columns: ColumnDef<Product>[] = [
    {
      accessorKey: "id",
      header: "id",
    },
    {
      accessorKey: "name",
      header: "name",
    },
    {
      accessorKey: "price",
      header: "Price",
      cell: ({ row }) => {
        const amount = parseFloat(row.getValue("price"))
        const formatted = new Intl.NumberFormat("fr-FR", {
          style: "currency",
          currency: "EUR",
        }).format(amount)
   
        return <div className="font-medium">{formatted}</div>
      },
    },
    {
      accessorKey: "stockInQuantity",
      header: "Stock",
    },
]

export function Products() {
  const navigate = useNavigate();

    const {handlePageChange, pageInfo, queryRes: {data, isLoading}} = useFetchDataWithPageable({table: "products"});
    
    const products: Product[] = data?.data.content || [];

    if (isLoading) return <div>Chargement...</div>;

    
    return (
        <>
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Products</h1>
        <Button 
        onClick={() => navigate('/products/new')}
        >
          <Plus className="mr-2 h-4 w-4" />
          Add Product
        </Button>
      </div>

      <div className="mt-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search products..."
              className="pl-8"
            />
          </div>
        </div>
        <DataTable columns={columns} data={products} onPageChange={handlePageChange} pageInfo={pageInfo}/>
      </div>
    </>
       
    )

  return (
    <>
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Products</h1>
        <Button 
        onClick={() => navigate('/products/new')}
        >
          <Plus className="mr-2 h-4 w-4" />
          Add Product
        </Button>
      </div>

      <div className="mt-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search products..."
              className="pl-8"
            />
          </div>
        </div>

        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>SKU</TableHead>
                <TableHead>Price</TableHead>
                <TableHead>Stock</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {products.map((product) => (
                <TableRow
                  key={product.id}
                  className="cursor-pointer"
                //   onClick={() => navigate(`/products/${product.id}`)}
                >
                  <TableCell className="font-medium">{product.name}</TableCell>
                  <TableCell>{product.sku}</TableCell>
                  <TableCell>${product.price.toFixed(2)}</TableCell>
                  <TableCell>{product.stock}</TableCell>
                  <TableCell>{product.status}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </div>
    </>
  );
}