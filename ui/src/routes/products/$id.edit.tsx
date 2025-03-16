import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/products/$id/edit')({
  component: RouteComponent,
})

function RouteComponent() {
  return <div>Hello "/products/$id/edit"!</div>
}
