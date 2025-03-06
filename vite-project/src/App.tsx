import { Dashboard } from "@/pages/dashboard";
import { Products } from "@/pages/products";
import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import { Layout } from "./components/layout";
import { Categories } from "./pages/categories";
import { CategoryForm } from "./pages/category-form";
import { Orders } from "./pages/orders";
import { ProductForm } from "./pages/product-form";
import { SignIn } from "./pages/sign-in";
// import { Orders } from "@/pages/orders"
// import { ProductForm } from "@/pages/product-form"

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-background">
      <div className="flex justify-end mb-4">

            </div>
          <Routes>
            <Route path="/sign-in" element={<SignIn />} />
            <Route element={<Layout />}>
              <Route path="/" element={<Dashboard />} />
              <Route path="/products" element={<Products />} />
              <Route path="/products/new" element={<ProductForm />} />
              <Route path="/products/:id" element={<ProductForm />} />
              <Route path="/categories" element={<Categories />} />
              <Route path="/categories/new" element={<CategoryForm />} />
              <Route path="/categories/:id" element={<CategoryForm />} />
              <Route path="/orders" element={<Orders />} />
            </Route>
          </Routes>
      </div>
    </Router>
  );
}

export default App