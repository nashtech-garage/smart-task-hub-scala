import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { store, persistor } from './store';
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
                    <AppRouter />
                    <ToastContainer
                        position="top-right"
                        autoClose={3000}
                        hideProgressBar={false}
                        newestOnTop={false}
                        closeOnClick
                        rtl={false}
                        pauseOnFocusLoss
                        draggable
                        pauseOnHover
                        theme="dark" // can switch to "dark"
                    />
                </PersistGate>
            </QueryClientProvider>
        </Provider>
    );
}

export default App;
