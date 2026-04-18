import React, { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { userApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import LoadingSpinner from '../components/common/LoadingSpinner';

const OAuth2CallbackPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const hasStarted = useRef(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (hasStarted.current) {
      return;
    }
    hasStarted.current = true;

    const token = searchParams.get('token');
    const oauthError = searchParams.get('error');

    if (oauthError) {
      setError('Social login failed. Please try again.');
      return;
    }
    if (!token) {
      setError('Missing OAuth token. Please try again.');
      return;
    }

    localStorage.setItem('wp_token', token);

    void userApi.getMe()
      .then((response) => {
        const profile = response.data.data;
        if (!profile) {
          throw new Error('Missing profile');
        }
        login(token, profile);
        navigate('/', { replace: true });
      })
      .catch(() => {
        localStorage.removeItem('wp_token');
        localStorage.removeItem('wp_user');
        setError('Unable to complete login. Please try again.');
      });
  }, [login, navigate, searchParams]);

  if (error) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center px-4">
        <div className="card p-8 w-full max-w-md text-center space-y-4">
          <h1 className="text-xl font-semibold text-gray-900">Login failed</h1>
          <p className="text-sm text-red-600">{error}</p>
          <Link to="/login" className="btn-primary inline-flex px-4 py-2">Back to Login</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[70vh] flex items-center justify-center px-4">
      <div className="card p-8 w-full max-w-md text-center space-y-4">
        <LoadingSpinner size="lg" />
        <h1 className="text-xl font-semibold text-gray-900">Signing you in...</h1>
        <p className="text-sm text-gray-500">Please wait while we complete your social login.</p>
      </div>
    </div>
  );
};

export default OAuth2CallbackPage;
