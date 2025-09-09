import Sidebar from "./components/Sidebar";
import { Outlet } from "react-router-dom";

const SidebarLayout = () => {
    return (
        <div className='flex h-full bg-gray-100'>
            {/* Sidebar */}
            <Sidebar />

            {/* Main Content */}
            <div className='flex-1 overflow-auto bg-[#1E2125]'>
                <Outlet />
            </div>
        </div>
    );
}

export default SidebarLayout;
