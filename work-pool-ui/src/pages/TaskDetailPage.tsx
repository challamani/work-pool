import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { taskApi } from '../api/tasks';
import { useAuthStore } from '../store/authStore';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { MapPin, IndianRupee, Clock, Star, CheckCircle, MessageCircle, Gavel } from 'lucide-react';
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

  if (isLoading) return <LoadingSpinner className="py-20" size="lg" />;
  if (!task) return (
    <div className="text-center py-20 space-y-2">
      <p className="text-2xl font-bold text-slate-300">Task not found</p>
    </div>
  );

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

  const statusGradient: Record<string, string> = {
    OPEN: 'from-emerald-500 to-teal-500',
    BIDDING: 'from-brand-500 to-indigo-500',
    ASSIGNED: 'from-violet-500 to-purple-500',
    IN_PROGRESS: 'from-amber-500 to-orange-400',
    PENDING_REVIEW: 'from-orange-500 to-flame-500',
    COMPLETED: 'from-slate-400 to-slate-500',
    CANCELLED: 'from-red-500 to-rose-500',
    DISPUTED: 'from-red-700 to-red-600',
    DRAFT: 'from-slate-400 to-slate-500',
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8 space-y-6">

      {/* Task header card */}
      <div className="card overflow-hidden">
        {/* Gradient header banner */}
        <div className={`bg-gradient-to-r ${statusGradient[task.status] ?? 'from-brand-500 to-indigo-500'} px-6 py-4 flex items-center justify-between`}>
          <span className="text-white/90 text-xs font-bold uppercase tracking-wider">{task.status.replace(/_/g, ' ')}</span>
          <span className="bg-white/20 backdrop-blur-sm text-white text-xs font-semibold px-3 py-1 rounded-full border border-white/30">
            {task.bidCount} bid{task.bidCount !== 1 ? 's' : ''}
          </span>
        </div>

        <div className="p-6 space-y-5">
          <h1 className="text-2xl font-extrabold text-slate-900 leading-tight">{task.title}</h1>
          <p className="text-slate-600 leading-relaxed">{task.description}</p>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="flex items-center gap-3 bg-emerald-50 rounded-xl p-3 border border-emerald-100">
              <IndianRupee className="w-5 h-5 text-emerald-600 flex-shrink-0" />
              <div>
                <p className="text-[10px] font-semibold text-emerald-500 uppercase">Budget</p>
                <p className="font-bold text-slate-800 text-sm">₹{task.budgetMin.toLocaleString('en-IN')} – ₹{task.budgetMax.toLocaleString('en-IN')}</p>
              </div>
            </div>
            {task.location && (
              <div className="flex items-center gap-3 bg-ocean-50/50 rounded-xl p-3 border border-cyan-100">
                <MapPin className="w-5 h-5 text-ocean-500 flex-shrink-0" />
                <div>
                  <p className="text-[10px] font-semibold text-ocean-500 uppercase">Location</p>
                  <p className="font-bold text-slate-800 text-sm">{task.location.city}, {task.location.state}</p>
                </div>
              </div>
            )}
            {task.scheduledStart && (
              <div className="flex items-center gap-3 bg-amber-50 rounded-xl p-3 border border-amber-100">
                <Clock className="w-5 h-5 text-amber-600 flex-shrink-0" />
                <div>
                  <p className="text-[10px] font-semibold text-amber-500 uppercase">Scheduled</p>
                  <p className="font-bold text-slate-800 text-sm">{new Date(task.scheduledStart).toLocaleDateString('en-IN')}</p>
                </div>
              </div>
            )}
          </div>

          {task.requiredSkills?.length > 0 && (
            <div className="flex flex-wrap gap-1.5">
              {task.requiredSkills.map((s) => (
                <span key={s} className="bg-brand-50 text-brand-700 text-xs px-3 py-1 rounded-full border border-brand-200 font-medium">{s}</span>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Bid section */}
      {canBid && (
        <div className="card p-6 space-y-4">
          <div className="flex items-center gap-2">
            <Gavel className="w-5 h-5 text-brand-500" />
            <h2 className="font-bold text-slate-900">Place a Bid</h2>
          </div>
          {!showBidForm ? (
            <button onClick={() => setShowBidForm(true)} className="btn-primary gap-2">
              <Gavel className="w-4 h-4" /> Bid on this Task
            </button>
          ) : (
            <form onSubmit={(e) => { e.preventDefault(); handleBidSubmit(); }} className="space-y-4">
              {error && (
                <div className="bg-red-50 text-red-700 border border-red-200 text-sm px-4 py-3 rounded-xl">{error}</div>
              )}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-semibold text-slate-700 mb-1.5">Your Price (₹)</label>
                  <input className="input" type="number" placeholder="e.g. 1500" min={1}
                    value={bidForm.proposedAmount} onChange={(e) => setBidForm({ ...bidForm, proposedAmount: e.target.value })} required />
                </div>
                <div>
                  <label className="block text-sm font-semibold text-slate-700 mb-1.5">Estimated Duration (hours)</label>
                  <input className="input" type="number" min={1} max={720}
                    value={bidForm.estimatedDurationHours} onChange={(e) => setBidForm({ ...bidForm, estimatedDurationHours: Number(e.target.value) })} required />
                </div>
              </div>
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1.5">Cover Note</label>
                <textarea className="input" rows={3} placeholder="Describe your approach..."
                  value={bidForm.coverNote} onChange={(e) => setBidForm({ ...bidForm, coverNote: e.target.value })} />
              </div>
              <div className="flex gap-3">
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
        <div className="card p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center gap-2">
          <CheckCircle className="w-4 h-4 text-emerald-600 flex-shrink-0" />
          Your bid has been submitted. You will be notified once the publisher reviews bids.
        </div>
      )}

      {/* Message */}
      {canMessage && (
        <div className="card p-6 space-y-4">
          <div className="flex items-center gap-2">
            <MessageCircle className="w-5 h-5 text-ocean-500" />
            <h2 className="font-bold text-slate-900">Message Participant</h2>
          </div>
          <p className="text-xs text-slate-400">Messaging is available only between the task publisher and assigned finisher.</p>
          <form
            onSubmit={(e) => { e.preventDefault(); setMessageStatus(''); messageMutation.mutate(messageText.trim()); }}
            className="space-y-3"
          >
            <textarea className="input" rows={3} placeholder="Write your message…"
              value={messageText} onChange={(e) => setMessageText(e.target.value)} required minLength={2} />
            <div className="flex items-center gap-3">
              <button type="submit" disabled={messageMutation.isPending} className="btn-secondary gap-1.5">
                <MessageCircle className="w-4 h-4" />
                {messageMutation.isPending ? <LoadingSpinner size="sm" className="inline" /> : 'Send'}
              </button>
              {messageStatus && <span className="text-sm text-slate-500">{messageStatus}</span>}
            </div>
          </form>
        </div>
      )}

      {/* Bids list (publisher only) */}
      {isPublisher && bids.length > 0 && (
        <div className="card p-6 space-y-4">
          <h2 className="font-bold text-slate-900">Received Bids <span className="text-brand-500">({bids.length})</span></h2>
          <div className="space-y-3">
            {bids.map((bid: Bid) => (
              <div key={bid.id} className="bg-gradient-card rounded-xl p-4 border border-brand-100 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-slate-900">{bid.finisherName}</span>
                  <span className="text-emerald-600 font-extrabold text-lg">₹{bid.proposedAmount.toLocaleString('en-IN')}</span>
                </div>
                {bid.coverNote && <p className="text-sm text-slate-600">{bid.coverNote}</p>}
                <p className="text-xs text-slate-400">⏱ Est. {bid.estimatedDurationHours} hours</p>
                {task.status !== 'ASSIGNED' && bid.status === 'PENDING' && (
                  <button
                    onClick={() => acceptBidMutation.mutate(bid.id)}
                    disabled={acceptBidMutation.isPending}
                    className="btn-primary text-sm py-1.5 gap-1"
                  >
                    <CheckCircle className="w-4 h-4" />
                    Accept Bid
                  </button>
                )}
                {bid.status === 'ACCEPTED' && (
                  <span className="badge bg-emerald-100 text-emerald-700">✓ Accepted</span>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Confirm completion (publisher) */}
      {isPublisher && task.status === 'PENDING_REVIEW' && (
        <div className="card p-6 space-y-4 border-l-4 border-flame-500 bg-amber-50/50">
          <h2 className="font-bold text-slate-900">Review & Confirm Completion</h2>
          <p className="text-sm text-slate-600">The finisher has marked this task as complete. Please review and confirm to release payment from escrow.</p>
          <div className="flex flex-wrap gap-3">
            <button onClick={() => confirmMutation.mutate()} disabled={confirmMutation.isPending} className="btn-primary">
              {confirmMutation.isPending ? <LoadingSpinner size="sm" className="inline" /> : '✅ Confirm & Release Payment'}
            </button>
            {task.assignedFinisherId && (
              <Link to={`/ratings/new?taskId=${task.id}&userId=${task.assignedFinisherId}`} className="btn-secondary gap-1.5">
                <Star className="w-4 h-4" />Rate Finisher
              </Link>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default TaskDetailPage;
