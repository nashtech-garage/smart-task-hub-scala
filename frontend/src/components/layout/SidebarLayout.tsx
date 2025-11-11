import Sidebar from "./components/Sidebar";
import { Outlet } from "react-router-dom";

const SidebarLayout = () => {
    return (
        <div className='flex h-full bg-[(var(--background))] text-[var(--foreground)]'>
            {/* Sidebar */}
            <Sidebar />

            {/* Main Content */}
            <div className='flex-1 overflow-auto bg-[(var(--background))] text-[var(--foreground)]'>
                <Outlet />
            </div>
        </div>
    );
}

export default SidebarLayout;
