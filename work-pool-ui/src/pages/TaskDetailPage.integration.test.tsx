import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { taskApi } from '../api/tasks';
import { useAuthStore } from '../store/authStore';
import type { Bid, Task } from '../types';
import TaskDetailPage from './TaskDetailPage';

vi.mock('../api/tasks', () => ({
  taskApi: {
    getTask: vi.fn(),
    getBids: vi.fn(),
    placeBid: vi.fn(),
    acceptBid: vi.fn(),
    confirmCompletion: vi.fn(),
    sendMessage: vi.fn(),
  },
}));

const baseTask: Task = {
  id: 'task-1',
  publisherId: 'publisher-1',
  title: 'Fix leaking tap',
  description: 'Need a plumber to fix leakage in kitchen sink.',
  category: 'PLUMBING',
  requiredSkills: ['plumbing'],
  location: {
    city: 'Pune',
    district: 'Pune',
    state: 'Maharashtra',
    latitude: 0,
    longitude: 0,
  },
  budgetMin: 800,
  budgetMax: 1800,
  status: 'OPEN',
  tags: [],
  bidCount: 0,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

const renderTaskDetailPage = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false, staleTime: 0 } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/tasks/task-1']}>
        <Routes>
          <Route path="/tasks/:id" element={<TaskDetailPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
};

const mockApiResponse = <T,>(data: T) => ({ data: { success: true, data } } as never);

describe('TaskDetailPage bidding integration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    useAuthStore.setState({
      token: null,
      user: null,
      isAuthenticated: false,
    });
  });

  it('allows a finisher to place a bid and shows confirmation state', async () => {
    useAuthStore.setState({
      token: 'token-1',
      user: {
        id: 'finisher-1',
        email: 'finisher@example.com',
        fullName: 'Finisher One',
        roles: ['FINISHER'],
        skills: [],
        serviceRadiusKm: 10,
        aadhaarVerification: 'UNVERIFIED',
        averageRating: 0,
        totalRatings: 0,
        emailVerified: true,
        createdAt: '2026-01-01T00:00:00Z',
      },
      isAuthenticated: true,
    });

    vi.mocked(taskApi.getTask).mockResolvedValue(mockApiResponse(baseTask));
    vi.mocked(taskApi.placeBid).mockResolvedValue(mockApiResponse({ id: 'bid-1' } as Bid));

    renderTaskDetailPage();

    await userEvent.click(await screen.findByRole('button', { name: 'Bid on this Task' }));
    await userEvent.type(screen.getByPlaceholderText('Describe your approach...'), 'Can complete this quickly and cleanly.');
    const [priceInput, durationInput] = screen.getAllByRole('spinbutton');
    await userEvent.clear(priceInput);
    await userEvent.type(priceInput, '1500');
    await userEvent.clear(durationInput);
    await userEvent.type(durationInput, '5');
    await userEvent.click(screen.getByRole('button', { name: 'Submit Bid' }));

    await waitFor(() => {
      expect(taskApi.placeBid).toHaveBeenCalledWith('task-1', {
        proposedAmount: 1500,
        coverNote: 'Can complete this quickly and cleanly.',
        estimatedDurationHours: 5,
      });
    });

    expect(
      await screen.findByText('Your bid has been submitted. You will be notified once the publisher reviews bids.'),
    ).toBeInTheDocument();
  });

  it('allows a publisher to accept a bid and refreshes bid state', async () => {
    useAuthStore.setState({
      token: 'token-1',
      user: {
        id: 'publisher-1',
        email: 'publisher@example.com',
        fullName: 'Publisher One',
        roles: ['PUBLISHER'],
        skills: [],
        serviceRadiusKm: 10,
        aadhaarVerification: 'UNVERIFIED',
        averageRating: 0,
        totalRatings: 0,
        emailVerified: true,
        createdAt: '2026-01-01T00:00:00Z',
      },
      isAuthenticated: true,
    });

    vi.mocked(taskApi.getTask).mockResolvedValue(mockApiResponse({ ...baseTask, status: 'BIDDING', bidCount: 1 }));
    vi.mocked(taskApi.getBids)
      .mockResolvedValueOnce(mockApiResponse([{
        id: 'bid-1',
        taskId: 'task-1',
        finisherId: 'finisher-1',
        finisherName: 'Finisher One',
        proposedAmount: 1200,
        coverNote: 'Can start tomorrow.',
        estimatedDurationHours: 6,
        status: 'PENDING',
        createdAt: '2026-01-01T00:00:00Z',
      } satisfies Bid]))
      .mockResolvedValueOnce(mockApiResponse([{
        id: 'bid-1',
        taskId: 'task-1',
        finisherId: 'finisher-1',
        finisherName: 'Finisher One',
        proposedAmount: 1200,
        coverNote: 'Can start tomorrow.',
        estimatedDurationHours: 6,
        status: 'ACCEPTED',
        createdAt: '2026-01-01T00:00:00Z',
      } satisfies Bid]));
    vi.mocked(taskApi.acceptBid).mockResolvedValue(mockApiResponse({ id: 'bid-1' } as Bid));

    renderTaskDetailPage();

    await userEvent.click(await screen.findByRole('button', { name: 'Accept Bid' }));

    await waitFor(() => {
      expect(taskApi.acceptBid).toHaveBeenCalledWith('task-1', 'bid-1');
    });
    expect(await screen.findByText('✓ Accepted')).toBeInTheDocument();
  });
});
