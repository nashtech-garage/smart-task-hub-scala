import type { LayoutProps } from '@/types/user.types';
import Navbar from './components/Navbar';
import { createContext, useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { RootState } from '@/store';
import { setTheme } from '@/store/slices/themeSlice';

type SearchContextType = {
    showSearch: boolean;
    setShowSearch: React.Dispatch<React.SetStateAction<boolean>>;
};

export const SearchContext = createContext<SearchContextType | null>(null);

export const MainLayout: React.FC<LayoutProps> = ({ children }) => {
    const [showSearch, setShowSearch] = useState(true);
    const dispatch = useDispatch();
    const theme = useSelector((state: RootState) => state.theme.mode);

    useEffect(() => {
        dispatch(setTheme(theme));
    })

    return (
        <SearchContext.Provider value={{ showSearch, setShowSearch }}>
            <div className="h-screen flex flex-col">
                <Navbar />
                <div className="flex-1 overflow-hidden">{children}</div>
            </div>
        </SearchContext.Provider>
    );
};
