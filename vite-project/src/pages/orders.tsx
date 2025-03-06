import { Badge } from "@/components/ui/badge";
import { DataTable } from "@/components/ui/data-table";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useFetchDataWithPageable } from '@/hooks/useFetchDataWithPageable';
import { Order } from "@/types/entities/order.entity";
import { ColumnDef } from "@tanstack/react-table";
import { Search } from "lucide-react";



const statusColors = {
  DELIVRED: "bg-green-100 text-green-800",
  CONFIRMED: "bg-blue-100 text-blue-800",
  SHIPPED: "bg-purple-100 text-purple-800",
  CANCELLED: "bg-red-100 text-red-800",
  PENDING: "bg-orange-100 text-orange-800",
};

const columns: ColumnDef<Order>[] = [
  {
    accessorKey: "id",
    header: "id",
  },
  {
    accessorKey: "orderDate",
    header: "Date",
    cell: ({ row }) => {
      const date = new Date(row.original.orderDate);

      return <p>{date.toLocaleDateString()} {date.toLocaleTimeString()}</p>
    },
  },
  {
    accessorKey: "price",
    header: "Price",
    cell: ({ row }) => {
      const status: unknown = row.original.status;

      return <Badge className={statusColors[status as keyof typeof statusColors]}>
        {status as string}
      </Badge>
    },
  },
  {
    accessorKey: "totalAmount",
    header: "Total",
    cell: ({ row }) => {
      const amount = parseFloat(row.getValue("totalAmount"))
      const formatted = new Intl.NumberFormat("fr-FR", {
        style: "currency",
        currency: "EUR",
      }).format(amount)
 
      return <div className="font-medium">{formatted}</div>
    },
  },
]


export function Orders() {

  const {queryRes: {data, isLoading}, handlePageChange, pageInfo} = useFetchDataWithPageable({table: "orders"})

  const orders: Order[] = data?.data.content || [];
  
  if(isLoading) return <p>Chargement ...</p>
  
  return (
    <>
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Orders</h1>
      </div>

      <div className="mt-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search orders..."
              className="pl-8"
            />
          </div>
          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Orders</SelectItem>
              <SelectItem value="completed">Completed</SelectItem>
              <SelectItem value="processing">Processing</SelectItem>
              <SelectItem value="shipped">Shipped</SelectItem>
              <SelectItem value="cancelled">Cancelled</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <DataTable columns={columns} data={orders} onPageChange={handlePageChange} pageInfo={pageInfo}/>
      </div>
    </>
  );
}