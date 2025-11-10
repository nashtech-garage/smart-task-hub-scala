import { Provider, useSelector } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { store, persistor, type RootState } from './store';
import LoadingFallback from './components/ui/LoadingFallback';
import { AppRouter } from './router/AppRouter';
import { ToastContainer } from "react-toastify";
import './index.css';
import { QueryClient, QueryClientProvider } from "@tanstack/react-query"

const queryClient = new QueryClient()

function App() {
    return (
        <Provider store={store}>
            <QueryClientProvider client={queryClient}>
                <PersistGate loading={<LoadingFallback />} persistor={persistor}>
                    <MainApp />
                </PersistGate>
            </QueryClientProvider>
        </Provider>
    );
}

function MainApp() {
    const theme = useSelector((state: RootState) => state.theme.mode);

    return (
        <>
            <AppRouter />
            <ToastContainer
                theme={theme}
                position="top-right"
                autoClose={3000}
                hideProgressBar={false}
                newestOnTop={false}
                closeOnClick
                pauseOnFocusLoss
                draggable
                pauseOnHover
            />
        </>
    );
}

export default App;
