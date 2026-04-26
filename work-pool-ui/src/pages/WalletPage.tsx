import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { paymentApi } from '../api/other';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { IndianRupee, Wallet, ArrowDownLeft, ArrowUpRight, TrendingUp } from 'lucide-react';

const statusColors: Record<string, string> = {
  PENDING:      'bg-amber-100 text-amber-700',
  ESCROW_HELD:  'bg-brand-100 text-brand-700',
  RELEASED:     'bg-emerald-100 text-emerald-700',
  REFUNDED:     'bg-slate-100 text-slate-600',
  FAILED:       'bg-red-100 text-red-700',
  DISPUTED:     'bg-orange-100 text-orange-700',
};

const WalletPage: React.FC = () => {
  const { data: walletData, isLoading: walletLoading } = useQuery({
    queryKey: ['wallet'],
    queryFn: paymentApi.getWallet,
  });

  const { data: historyData, isLoading: historyLoading } = useQuery({
    queryKey: ['transactions', true],
    queryFn: () => paymentApi.getHistory(true),
  });

  const wallet = walletData?.data?.data;
  const transactions = historyData?.data?.data ?? [];

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-6">
      <div>
        <h1 className="section-title">Wallet</h1>
        <p className="text-slate-500 text-sm mt-1">Track your balance and transactions</p>
      </div>

      {walletLoading ? (
        <LoadingSpinner className="py-10" size="lg" />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {/* Available balance */}
          <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-brand-600 to-indigo-700 p-6 text-white shadow-brand-lg">
            <div className="absolute top-0 right-0 w-32 h-32 rounded-full bg-white/10 -translate-y-1/2 translate-x-1/4" />
            <Wallet className="w-6 h-6 mb-3 opacity-80" />
            <p className="text-sm font-semibold opacity-80 mb-1">Available Balance</p>
            <p className="text-3xl font-extrabold">
              ₹{(wallet?.balance ?? 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
          </div>
          {/* Escrow */}
          <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-ocean-500 to-cyan-600 p-6 text-white shadow-lg">
            <div className="absolute top-0 right-0 w-32 h-32 rounded-full bg-white/10 -translate-y-1/2 translate-x-1/4" />
            <TrendingUp className="w-6 h-6 mb-3 opacity-80" />
            <p className="text-sm font-semibold opacity-80 mb-1">In Escrow</p>
            <p className="text-3xl font-extrabold">
              ₹{(wallet?.escrowBalance ?? 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
          </div>
        </div>
      )}

      <div className="space-y-3">
        <h2 className="font-bold text-slate-900">Transaction History</h2>
        {historyLoading ? (
          <LoadingSpinner className="py-10" />
        ) : transactions.length === 0 ? (
          <div className="text-center py-14 space-y-2">
            <IndianRupee className="w-10 h-10 mx-auto text-slate-200" />
            <p className="text-slate-400 text-sm font-medium">No transactions yet</p>
          </div>
        ) : (
          transactions.map((t) => (
            <div key={t.id} className="card p-4 flex items-center gap-3 hover:shadow-card-hover transition-shadow">
              <div className={`w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0 ${
                t.status === 'RELEASED' ? 'bg-emerald-100' : 'bg-brand-100'
              }`}>
                {t.status === 'RELEASED'
                  ? <ArrowDownLeft className="w-4 h-4 text-emerald-600" />
                  : <ArrowUpRight className="w-4 h-4 text-brand-600" />}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-slate-900 truncate">Task: {t.taskId}</p>
                <p className="text-xs text-slate-400">
                  Commission: ₹{t.publisherCommission?.toFixed(2)} + ₹{t.finisherCommission?.toFixed(2)}
                </p>
                <p className="text-xs text-slate-300">
                  {new Date(t.createdAt).toLocaleDateString('en-IN')}
                </p>
              </div>
              <div className="text-right flex-shrink-0">
                <p className="font-extrabold text-slate-900">₹{t.agreedAmount?.toLocaleString('en-IN')}</p>
                <span className={`badge text-[10px] font-bold ${statusColors[t.status] ?? 'bg-slate-100 text-slate-600'}`}>
                  {t.status}
                </span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default WalletPage;
