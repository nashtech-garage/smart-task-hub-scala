import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { store, persistor } from './store';
import LoadingFallback from './components/ui/LoadingFallback';
import { AppRouter } from './router/AppRouter';
import { ToastContainer } from "react-toastify";
import './index.css';

function App() {
    return (
        <Provider store={store}>
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
        </Provider>
    );
}

export default App;
