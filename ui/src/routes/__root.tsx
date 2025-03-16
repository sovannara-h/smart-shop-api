import { AppSidebar } from '@/components/app-sidebar'
import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
import { createRootRoute, Outlet } from '@tanstack/react-router'

export const Route = createRootRoute({
  component: () => (
    <SidebarProvider>
    <AppSidebar />
    <main className='flex-1 p-6'>
      <SidebarTrigger />
      <Outlet />
    </main>
  </SidebarProvider>
  ),
})