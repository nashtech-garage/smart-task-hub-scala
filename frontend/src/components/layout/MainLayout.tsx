import type { LayoutProps } from '@/types/user.types';
import Navbar from './components/Navbar';
import { createContext, useState } from 'react';

type SearchContextType = {
    showSearch: boolean;
    setShowSearch: React.Dispatch<React.SetStateAction<boolean>>;
};

export const SearchContext = createContext<SearchContextType | null>(null);

export const MainLayout: React.FC<LayoutProps> = ({ children }) => {
    const [showSearch, setShowSearch] = useState(true);

    return (
        <SearchContext.Provider value={{ showSearch, setShowSearch }}>
            <div className="h-screen flex flex-col">
                <Navbar />
                <div className="flex-1 overflow-hidden">{children}</div>
            </div>
        </SearchContext.Provider>
    );
};
