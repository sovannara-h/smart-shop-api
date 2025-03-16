import { productsApi } from '@/features/products/api/product.api'
import { ProductForm } from '@/features/products/components/product-form'
import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/products/$id')({
  component: RouteComponent,
  loader: async({ params }) => {
    const id = params.id;
    const product = await productsApi.getProduct(params.id)
    return product
  }
})

function RouteComponent() {
  return <ProductForm />
}
