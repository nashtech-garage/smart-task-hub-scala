import type { LayoutProps } from '@/types/user.types';
import Navbar from './components/Navbar';

export const MainLayout: React.FC<LayoutProps> = ({ children }) => {
    return (
        <div className='h-screen flex flex-col'>
            <Navbar />
            <div className='flex-1 overflow-hidden'>{children}</div>
        </div>
    );
};
