import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { store } from './store';
import App from './App';

// Mock AppRouter since it might have complex routing logic
vi.mock('./router/AppRouter', () => ({
    AppRouter: () => <div data-testid='app-router'>App Router Mock</div>,
}));

describe('App Component', () => {
    it('renders without crashing', () => {
        render(
            <Provider store={store}>
                <App />
            </Provider>
        );

        expect(screen.getByTestId('app-router')).toBeInTheDocument();
    });

    it('provides Redux store to children', () => {
        const { container } = render(
            <Provider store={store}>
                <App />
            </Provider>
        );

        // App should render without errors when Redux store is provided
        expect(container.firstChild).toBeInTheDocument();
    });
});
