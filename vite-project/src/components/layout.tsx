
import { Outlet } from 'react-router-dom';
import { AppSidebar } from "./app-sidebar";
import { SidebarProvider } from "./ui/sidebar";

export type  LayoutProps = {

}

export const Layout = (props: LayoutProps) => {


    return (
      <SidebarProvider>
      <div  className="flex flex-1">
        {/* <header className="border-b"> */}
          <AppSidebar />
          {/* <div className="container flex h-16 items-center px-4">
            <NavigationMenu>
              <NavigationMenuList aria-orientation="vertical" className="">
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
          </div> */}
        {/* </header> */}
        <main className="px-4 py-8 flex-1">
          <Outlet />
        </main>
      </div>
      </SidebarProvider>
    );
}