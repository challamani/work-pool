import React, { Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/common/ProtectedRoute';
import LoadingSpinner from './components/common/LoadingSpinner';

const HomePage = React.lazy(() => import('./pages/HomePage'));
const LoginPage = React.lazy(() => import('./pages/LoginPage'));
const RegisterPage = React.lazy(() => import('./pages/RegisterPage'));
const OAuth2CallbackPage = React.lazy(() => import('./pages/OAuth2CallbackPage'));
const TasksPage = React.lazy(() => import('./pages/TasksPage'));
const TaskDetailPage = React.lazy(() => import('./pages/TaskDetailPage'));
const PostTaskPage = React.lazy(() => import('./pages/PostTaskPage'));
const ProfilePage = React.lazy(() => import('./pages/ProfilePage'));
const NotificationsPage = React.lazy(() => import('./pages/NotificationsPage'));
const WalletPage = React.lazy(() => import('./pages/WalletPage'));

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 30_000 } },
});

const App: React.FC = () => (
  <QueryClientProvider client={queryClient}>
    <BrowserRouter>
      <Suspense fallback={<LoadingSpinner className="min-h-screen" size="lg" />}>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/auth/oauth2/callback" element={<OAuth2CallbackPage />} />
            <Route path="/tasks" element={<TasksPage />} />
            <Route path="/tasks/:id" element={<TaskDetailPage />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/tasks/new" element={<PostTaskPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/notifications" element={<NotificationsPage />} />
              <Route path="/wallet" element={<WalletPage />} />
            </Route>
            <Route path="*" element={
              <div className="text-center py-24 space-y-4">
                <p className="text-7xl font-extrabold text-gradient">404</p>
                <p className="text-xl font-bold text-slate-700">Page not found</p>
                <p className="text-slate-400 text-sm">The page you're looking for doesn't exist.</p>
                <a href="/" className="btn-primary inline-flex mt-2 px-6 py-2.5">Go Home</a>
              </div>
            } />
          </Route>
        </Routes>
      </Suspense>
    </BrowserRouter>
  </QueryClientProvider>
);

export default App;
