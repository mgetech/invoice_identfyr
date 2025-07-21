
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import axios from 'axios';
import InvoiceCategorizer from './InvoiceCategorizer';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('InvoiceCategorizer', () => {
    it('renders the form and handles successful submission', async () => {
        mockedAxios.post.mockResolvedValue({ data: 'Category 31' });

        render(<InvoiceCategorizer />);

        const input = screen.getByLabelText(/invoice item description/i);
        const button = screen.getByRole('button', { name: /categorize/i });

        fireEvent.change(input, { target: { value: 'Allgemeine Hund Besuchung' } });
        fireEvent.click(button);

        await waitFor(() => {
            expect(screen.getByText(/category: category 31/i)).toBeInTheDocument();
        });
    });

    it('shows an error message when the input is empty', async () => {
        render(<InvoiceCategorizer />);

        const button = screen.getByRole('button', { name: /categorize/i });
        fireEvent.click(button);

        await waitFor(() => {
            expect(screen.getByText(/please enter a description./i)).toBeInTheDocument();
        });
    });

    it('shows an error message when the api call fails', async () => {
        mockedAxios.post.mockRejectedValue(new Error('Network Error'));

        render(<InvoiceCategorizer />);

        const input = screen.getByLabelText(/invoice item description/i);
        const button = screen.getByRole('button', { name: /categorize/i });

        fireEvent.change(input, { target: { value: 'Allgemeine Hund Besuchung' } });
        fireEvent.click(button);

        await waitFor(() => {
            expect(screen.getByText(/an error occurred while categorizing the invoice./i)).toBeInTheDocument();
        });
    });
});
