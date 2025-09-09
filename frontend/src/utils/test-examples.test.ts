import { describe, it, expect } from 'vitest';
import { createSlice } from '@reduxjs/toolkit';

// Simple utility function test
export const calculateTotal = (items: number[]): number => {
    return items.reduce((sum, item) => sum + item, 0);
};

// Test for Redux slice (example)
const counterSlice = createSlice({
    name: 'counter',
    initialState: { value: 0 },
    reducers: {
        increment: state => {
            state.value += 1;
        },
        decrement: state => {
            state.value -= 1;
        },
    },
});

describe('Utility Functions', () => {
    it('should calculate total correctly', () => {
        expect(calculateTotal([1, 2, 3, 4])).toBe(10);
        expect(calculateTotal([])).toBe(0);
        expect(calculateTotal([5])).toBe(5);
    });
});

describe('Counter Slice', () => {
    it('should increment value', () => {
        const initialState = { value: 0 };
        const action = counterSlice.actions.increment();
        const nextState = counterSlice.reducer(initialState, action);

        expect(nextState.value).toBe(1);
    });

    it('should decrement value', () => {
        const initialState = { value: 5 };
        const action = counterSlice.actions.decrement();
        const nextState = counterSlice.reducer(initialState, action);

        expect(nextState.value).toBe(4);
    });
});
