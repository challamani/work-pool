import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { paymentApi } from '../api/other';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { IndianRupee, Wallet, ArrowDownLeft, ArrowUpRight } from 'lucide-react';

const statusColors: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  ESCROW_HELD: 'bg-blue-100 text-blue-700',
  RELEASED: 'bg-green-100 text-green-700',
  REFUNDED: 'bg-gray-100 text-gray-600',
  FAILED: 'bg-red-100 text-red-700',
  DISPUTED: 'bg-orange-100 text-orange-700',
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
      <h1 className="text-2xl font-bold text-gray-900">Wallet</h1>

      {walletLoading ? (
        <LoadingSpinner />
      ) : (
        <div className="grid grid-cols-2 gap-4">
          <div className="card p-5 space-y-1">
            <div className="flex items-center gap-2 text-gray-500 text-sm">
              <Wallet className="w-4 h-4" /> Available Balance
            </div>
            <p className="text-2xl font-bold text-gray-900">
              ₹{(wallet?.balance ?? 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div className="card p-5 space-y-1">
            <div className="flex items-center gap-2 text-gray-500 text-sm">
              <IndianRupee className="w-4 h-4" /> In Escrow
            </div>
            <p className="text-2xl font-bold text-blue-600">
              ₹{(wallet?.escrowBalance ?? 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </p>
          </div>
        </div>
      )}

      <div className="space-y-3">
        <h2 className="font-semibold text-gray-900">Transaction History</h2>
        {historyLoading ? (
          <LoadingSpinner />
        ) : transactions.length === 0 ? (
          <p className="text-gray-500 text-sm text-center py-8">No transactions yet</p>
        ) : (
          transactions.map((t) => (
            <div key={t.id} className="card p-4 flex items-center gap-3">
              <div className={`p-2 rounded-full ${t.status === 'RELEASED' ? 'bg-green-100' : 'bg-blue-100'}`}>
                {t.status === 'RELEASED'
                  ? <ArrowDownLeft className="w-4 h-4 text-green-600" />
                  : <ArrowUpRight className="w-4 h-4 text-blue-600" />}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">Task: {t.taskId}</p>
                <p className="text-xs text-gray-500">
                  Commission: ₹{t.publisherCommission?.toFixed(2)} + ₹{t.finisherCommission?.toFixed(2)}
                </p>
                <p className="text-xs text-gray-400">
                  {new Date(t.createdAt).toLocaleDateString('en-IN')}
                </p>
              </div>
              <div className="text-right">
                <p className="font-semibold text-gray-900">₹{t.agreedAmount?.toLocaleString('en-IN')}</p>
                <span className={`badge text-xs ${statusColors[t.status] ?? 'bg-gray-100 text-gray-600'}`}>{t.status}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default WalletPage;
