import { Overview } from "@/components/dashboard/overview-chart";
import { RecentSales } from "@/components/dashboard/recent-sales";
import { StatsCard } from "@/components/dashboard/stats-card";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Order } from "@/types/entities/order.entity";
import { useQuery } from "@tanstack/react-query";
import axios from 'axios';
import { BarChart3, Package, ShoppingCart, Users } from 'lucide-react';

export function Dashboard() {

  const month = new Date().getMonth() + 1; // +1 car getMonth() retourne 0-11

  const {data: ordersData} = useQuery({
    queryKey: ["orders", month],
    queryFn: async () => {
      const response = await axios.get(`http://localhost:8080/api/orders/month?month=${month}`);
      return response.data;
    }
  });

  const { data: productCount } = useQuery({
    queryKey: ["productCount"],
    queryFn: async () => {
        const response = await axios.get("http://localhost:8080/api/products/count");
        return response.data.data;
    }
});



  const orders = ordersData?.data;
  console.log(orders, productCount)


  const convertIntToCurrency = (amount: number) => {
    return new Intl.NumberFormat("fr-FR", {
      style: "currency",
      currency: "EUR",
    }).format(amount)
  }

  const getTotalRevenue = (orders: Order[]) => {
    if(!orders?.length) return convertIntToCurrency(0);
    const total = orders.reduce((a,c) => c.totalAmount + a, 0)
    return convertIntToCurrency(total);
  }
  return (
    <>
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
      </div>
      
      <Tabs defaultValue="overview" className="mt-6 space-y-6">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="analytics">Analytics</TabsTrigger>
          <TabsTrigger value="reports">Reports</TabsTrigger>
        </TabsList>
        
        <TabsContent value="overview" className="space-y-6">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <StatsCard
              title="Total Revenue"
              value={getTotalRevenue(orders)}
              icon={<BarChart3 className="h-4 w-4 text-muted-foreground" />}
              description="+20.1% from last month"
            />
            <StatsCard
              title="Orders"
              value={orders?.length}
              icon={<ShoppingCart className="h-4 w-4 text-muted-foreground" />}
              description="+180 this week"
            />
            <StatsCard
              title="Products"
              value={productCount.toLocaleString('en-US')}
              icon={<Package className="h-4 w-4 text-muted-foreground" />}
              description="86 added today"
            />
            <StatsCard
              title="Active Users"
              value="573"
              icon={<Users className="h-4 w-4 text-muted-foreground" />}
              description="+201 since last hour"
            />
          </div>

          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
            <Card className="col-span-4">
              <CardHeader>
                <CardTitle>Overview</CardTitle>
                <CardDescription>
                  Daily revenue overview for the past week
                </CardDescription>
              </CardHeader>
              <CardContent className="pl-2">
                <Overview />
              </CardContent>
            </Card>
            
            <Card className="col-span-3">
              <CardHeader>
                <CardTitle>Recent Sales</CardTitle>
                <CardDescription>
                  Latest transactions from your store
                </CardDescription>
              </CardHeader>
              <CardContent>
                <RecentSales />
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </>
  );
}