import { NavigationMenu, NavigationMenuItem, NavigationMenuList, navigationMenuTriggerStyle } from "@/components/ui/navigation-menu";
import { Dashboard } from "@/pages/dashboard";
import { Products } from "@/pages/products";
import { Link, Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import { Orders } from "./pages/orders";
import { ProductForm } from "./pages/product-form";
// import { Orders } from "@/pages/orders"
// import { ProductForm } from "@/pages/product-form"

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-background">
        <header className="border-b">
          <div className="container flex h-16 items-center px-4">
            <NavigationMenu>
              <NavigationMenuList>
                <NavigationMenuItem>
                  <Link to="/" className={navigationMenuTriggerStyle()}>
                    Dashboard
                  </Link>
                </NavigationMenuItem>
                <NavigationMenuItem>
                  <Link to="/products" className={navigationMenuTriggerStyle()}>
                    Products
                  </Link>
                </NavigationMenuItem>
                <NavigationMenuItem>
                  <Link to="/orders" className={navigationMenuTriggerStyle()}>
                    Orders
                  </Link>
                </NavigationMenuItem>
              </NavigationMenuList>
            </NavigationMenu>
          </div>
        </header>
        
        <main className="px-4 py-8">

          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/products" element={<Products />} />
            <Route path="/products/new" element={<ProductForm />} />
            <Route path="/products/:id" element={<ProductForm />} />
            <Route path="/orders" element={<Orders />} />
          </Routes>

        </main>
      </div>
    </Router>
  );
}

export default App