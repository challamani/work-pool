import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { taskApi } from '../api/tasks';
import { useAuthStore } from '../store/authStore';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { MapPin, IndianRupee, Clock, Star, CheckCircle } from 'lucide-react';
import type { ApiResponse, Bid } from '../types';

const TaskDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user, isAuthenticated } = useAuthStore();
  const queryClient = useQueryClient();
  const [bidForm, setBidForm] = useState({ proposedAmount: '', coverNote: '', estimatedDurationHours: 4 });
  const [showBidForm, setShowBidForm] = useState(false);
  const [hasPlacedBid, setHasPlacedBid] = useState(false);
  const [messageText, setMessageText] = useState('');
  const [messageStatus, setMessageStatus] = useState('');
  const [error, setError] = useState('');

  const { data: taskData, isLoading } = useQuery({
    queryKey: ['task', id],
    queryFn: () => taskApi.getTask(id!),
    enabled: !!id,
  });

  const task = taskData?.data?.data;

  const { data: bidsData } = useQuery({
    queryKey: ['bids', id],
    queryFn: () => taskApi.getBids(id!),
    enabled: !!task && task.publisherId === user?.id,
  });

  const bids = bidsData?.data?.data ?? [];

  const bidMutation = useMutation({
    mutationFn: () => taskApi.placeBid(id!, {
      proposedAmount: Number(bidForm.proposedAmount),
      coverNote: bidForm.coverNote,
      estimatedDurationHours: bidForm.estimatedDurationHours,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task', id] });
      setHasPlacedBid(true);
      setBidForm({ proposedAmount: '', coverNote: '', estimatedDurationHours: 4 });
      setShowBidForm(false);
      setError('');
    },
    onError: (err) => {
      const apiError = err as AxiosError<ApiResponse<null>>;
      setError(apiError.response?.data?.message || 'Failed to place bid');
    },
  });

  const acceptBidMutation = useMutation({
    mutationFn: (bidId: string) => taskApi.acceptBid(id!, bidId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task', id] });
      queryClient.invalidateQueries({ queryKey: ['bids', id] });
    },
  });

  const confirmMutation = useMutation({
    mutationFn: () => taskApi.confirmCompletion(id!),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['task', id] }),
  });

  const messageMutation = useMutation({
    mutationFn: (message: string) => {
      if (!task || !user?.id) {
        return Promise.reject(new Error('Task or user is missing'));
      }
      const recipientUserId =
        user.id === task.publisherId ? task.assignedFinisherId : task.publisherId;
      if (!recipientUserId) {
        return Promise.reject(new Error('Recipient unavailable'));
      }
      return taskApi.sendMessage(task.id, recipientUserId, message);
    },
    onSuccess: () => {
      setMessageText('');
      setMessageStatus('Message sent');
    },
    onError: (err) => {
      const apiError = err as AxiosError<ApiResponse<null>>;
      setMessageStatus(apiError.response?.data?.message || 'Failed to send message');
    },
  });

  if (isLoading) return <LoadingSpinner className="py-20" />;
  if (!task) return <div className="text-center py-20 text-gray-500">Task not found</div>;

  const handleBidSubmit = () => {
    const proposedAmount = Number(bidForm.proposedAmount);
    const durationHours = Number(bidForm.estimatedDurationHours);
    if (!Number.isFinite(proposedAmount) || proposedAmount < 1) {
      setError('Please enter a valid bid amount');
      return;
    }
    if (!Number.isFinite(durationHours) || durationHours < 1 || durationHours > 720) {
      setError('Estimated duration must be between 1 and 720 hours');
      return;
    }
    setError('');
    bidMutation.mutate();
  };

  const isPublisher = user?.id === task.publisherId;
  const isAssignedFinisher = user?.id === task.assignedFinisherId;
  const canBid = isAuthenticated && !isPublisher && !hasPlacedBid && (task.status === 'OPEN' || task.status === 'BIDDING');
  const canMessage = isAuthenticated && (isPublisher || isAssignedFinisher) && !!task.assignedFinisherId;

  return (
    <div className="max-w-4xl mx-auto px-4 py-6 space-y-6">
      {/* Task header */}
      <div className="card p-6 space-y-4">
        <div className="flex items-start justify-between gap-4">
          <h1 className="text-2xl font-bold text-gray-900">{task.title}</h1>
          <span className={`badge ${task.status === 'OPEN' ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'}`}>
            {task.status}
          </span>
        </div>

        <p className="text-gray-700">{task.description}</p>

        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 text-sm">
          <div className="flex items-center gap-2 text-gray-600">
            <IndianRupee className="w-4 h-4 text-green-600" />
            <span>₹{task.budgetMin.toLocaleString('en-IN')} – ₹{task.budgetMax.toLocaleString('en-IN')}</span>
          </div>
          {task.location && (
            <div className="flex items-center gap-2 text-gray-600">
              <MapPin className="w-4 h-4 text-blue-600" />
              <span>{task.location.city}, {task.location.district}, {task.location.state}</span>
            </div>
          )}
          {task.scheduledStart && (
            <div className="flex items-center gap-2 text-gray-600">
              <Clock className="w-4 h-4 text-orange-500" />
              <span>{new Date(task.scheduledStart).toLocaleDateString('en-IN')}</span>
            </div>
          )}
        </div>

        {task.requiredSkills?.length > 0 && (
          <div className="flex flex-wrap gap-1">
            {task.requiredSkills.map((s) => (
              <span key={s} className="bg-blue-50 text-blue-700 text-xs px-2 py-0.5 rounded-full">{s}</span>
            ))}
          </div>
        )}
      </div>

      {/* Bid section */}
      {canBid && (
        <div className="card p-6 space-y-3">
          <h2 className="font-semibold text-gray-900">Place a Bid</h2>
          {!showBidForm ? (
            <button onClick={() => setShowBidForm(true)} className="btn-primary">
              Bid on this Task
            </button>
          ) : (
            <form onSubmit={(e) => { e.preventDefault(); handleBidSubmit(); }} className="space-y-3">
              {error && <div className="text-red-600 text-sm">{error}</div>}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Your Price (₹)</label>
                <input className="input" type="number" placeholder="e.g. 1500" min={1}
                  value={bidForm.proposedAmount} onChange={(e) => setBidForm({ ...bidForm, proposedAmount: e.target.value })} required />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Cover Note</label>
                <textarea className="input" rows={3} placeholder="Describe your approach..."
                  value={bidForm.coverNote} onChange={(e) => setBidForm({ ...bidForm, coverNote: e.target.value })} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Estimated Duration (hours)</label>
                <input className="input" type="number" min={1} max={720}
                  value={bidForm.estimatedDurationHours} onChange={(e) => setBidForm({ ...bidForm, estimatedDurationHours: Number(e.target.value) })} required />
              </div>
              <div className="flex gap-2">
                <button type="submit" disabled={bidMutation.isPending} className="btn-primary">
                  {bidMutation.isPending ? <LoadingSpinner size="sm" className="inline" /> : 'Submit Bid'}
                </button>
                <button type="button" onClick={() => { setShowBidForm(false); setError(''); }} className="btn-secondary">Cancel</button>
              </div>
            </form>
          )}
        </div>
      )}
      {hasPlacedBid && (
        <div className="card p-4 text-sm text-emerald-700 bg-emerald-50 border-emerald-200">
          Your bid has been submitted. You will be notified once the publisher reviews bids.
        </div>
      )}

      {canMessage && (
        <div className="card p-6 space-y-3">
          <h2 className="font-semibold text-gray-900">Message participant</h2>
          <p className="text-xs text-gray-500">
            Messaging is available only between the task publisher and assigned finisher.
          </p>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              setMessageStatus('');
              messageMutation.mutate(messageText.trim());
            }}
            className="space-y-3"
          >
            <textarea
              className="input"
              rows={3}
              placeholder="Write your message"
              value={messageText}
              onChange={(e) => setMessageText(e.target.value)}
              required
              minLength={2}
            />
            <div className="flex items-center gap-3">
              <button type="submit" disabled={messageMutation.isPending} className="btn-secondary">
                {messageMutation.isPending ? <LoadingSpinner size="sm" className="inline" /> : 'Send Message'}
              </button>
              {messageStatus && <span className="text-sm text-gray-600">{messageStatus}</span>}
            </div>
          </form>
        </div>
      )}

      {/* Bids list (publisher only) */}
      {isPublisher && bids.length > 0 && (
        <div className="card p-6 space-y-4">
          <h2 className="font-semibold text-gray-900">Received Bids ({bids.length})</h2>
          {bids.map((bid: Bid) => (
            <div key={bid.id} className="border border-gray-100 rounded-lg p-4 space-y-2">
              <div className="flex items-center justify-between">
                <span className="font-medium text-gray-900">{bid.finisherName}</span>
                <span className="text-green-600 font-semibold">₹{bid.proposedAmount.toLocaleString('en-IN')}</span>
              </div>
              <p className="text-sm text-gray-600">{bid.coverNote}</p>
              <p className="text-xs text-gray-400">Est. {bid.estimatedDurationHours} hours</p>
              {task.status !== 'ASSIGNED' && bid.status === 'PENDING' && (
                <button
                  onClick={() => acceptBidMutation.mutate(bid.id)}
                  disabled={acceptBidMutation.isPending}
                  className="btn-primary text-sm py-1.5"
                >
                  <CheckCircle className="w-4 h-4 mr-1 inline" />
                  Accept Bid
                </button>
              )}
              {bid.status === 'ACCEPTED' && (
                <span className="badge bg-green-100 text-green-700">✓ Accepted</span>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Confirm completion (publisher) */}
      {isPublisher && task.status === 'PENDING_REVIEW' && (
        <div className="card p-6 space-y-3 border-l-4 border-orange-400">
          <h2 className="font-semibold text-gray-900">Review & Confirm Completion</h2>
          <p className="text-sm text-gray-600">The finisher has marked this task as complete. Please review and confirm to release payment.</p>
          <button onClick={() => confirmMutation.mutate()} disabled={confirmMutation.isPending} className="btn-primary">
            {confirmMutation.isPending ? <LoadingSpinner size="sm" className="inline" /> : 'Confirm & Release Payment'}
          </button>
          {task.assignedFinisherId && (
            <Link to={`/ratings/new?taskId=${task.id}&userId=${task.assignedFinisherId}`} className="btn-secondary block text-center mt-2">
              <Star className="w-4 h-4 inline mr-1" />Rate Finisher
            </Link>
          )}
        </div>
      )}
    </div>
  );
};

export default TaskDetailPage;
