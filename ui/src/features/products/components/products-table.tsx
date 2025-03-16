import { DataTable } from "@/components/ui/data-table"
import { ColumnDef } from "@tanstack/react-table"
import { Product } from "../types/product.types"


const columns: ColumnDef<Product>[] = [
  {
    accessorKey: "name",
    header: "Nom"
  },
  {
    accessorKey: "price",
    header: "Prix",
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue("price"))
      return new Intl.NumberFormat("fr-FR", {
        style: "currency",
        currency: "EUR"
      }).format(amount)
    }
  },
  {
    accessorKey: "stockInQuantity",
    header: "Stock"
  }
]

interface ProductTableProps {
  data: Product[]
  pageInfo: {
    currentPage: number
    pageSize: number
    totalPages: number
    totalElements: number
  }
  onPageChange: (page: number) => void
}

export function ProductTable({ data, pageInfo, onPageChange }: ProductTableProps) {
  return (
    <DataTable 
      columns={columns} 
      data={data} 
      pageInfo={pageInfo}
      onPageChange={onPageChange}
    />
  )
}