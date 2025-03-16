import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { productsApi } from '@/features/products/api/product.api'
import { ProductTable } from '@/features/products/components/products-table'
import { createFileRoute } from '@tanstack/react-router'
import { Plus, Search } from 'lucide-react'
import { z } from 'zod'

const searchSchema = z.object({
    page: z.number().default(0),
    size: z.number().default(10),
    search: z.string().optional()
  })

type SearchType = z.infer<typeof searchSchema>

export const Route = createFileRoute('/products/')({
    validateSearch: searchSchema,
    loaderDeps: ({ search }: { search: SearchType }) => ({ search }),
    loader: async ({ deps }) => await productsApi.getProducts(deps.search),
    component: RouteComponent,
})

function RouteComponent() {
    const { data } = Route.useLoaderData()
  const navigate = Route.useNavigate()
  const { search } = Route.useSearch()


  return <div>
    <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Produits</h1>
        <Button onClick={() => navigate({ to: '/products/new' })}>
          <Plus className="mr-2 h-4 w-4" />
          Nouveau Produit
        </Button>
      </div>
      <div className="mt-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Rechercher..."
              className="pl-8"
            //   value={search.search || ''}
              onChange={(e) => {
                navigate({
                  search: (prev) => ({ ...prev, search: e.target.value })
                })
              }}
            />
          </div>
        </div>
    <ProductTable 
          data={data.content}
          pageInfo={{
            currentPage: search?.page,
            pageSize: search?.size,
            totalPages: data.totalPages,
            totalElements: data.totalElements
          }}
          onPageChange={(page) => {
            navigate({
              search: (prev) => ({ ...prev, page })
            })
          }}
        />
        </div>
  </div>
}
