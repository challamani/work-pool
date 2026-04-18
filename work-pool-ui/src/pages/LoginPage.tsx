import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Briefcase } from 'lucide-react';

const LoginPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const hasOauthError = !!searchParams.get('error');

  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4">
      <div className="card p-8 w-full max-w-sm space-y-6">
        <div className="text-center">
          <div className="flex justify-center mb-3">
            <Briefcase className="w-10 h-10 text-blue-600" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Welcome back</h1>
          <p className="text-sm text-gray-500">Continue with Google or Facebook</p>
        </div>

        {hasOauthError && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-3 py-2 rounded-lg">
            Social login failed. Please try again.
          </div>
        )}

        <div className="grid grid-cols-1 gap-3">
          <a href={`${apiBase}/api/v1/auth/oauth2/authorization/google`}
            className="btn-secondary text-sm py-2 flex items-center justify-center gap-2">
            <img src="https://www.google.com/favicon.ico" alt="Google" className="w-4 h-4" />
            Continue with Google
          </a>
          <a href={`${apiBase}/api/v1/auth/oauth2/authorization/facebook`}
            className="btn-secondary text-sm py-2 flex items-center justify-center gap-2">
            <span className="text-blue-600 font-bold text-sm">f</span>
            Continue with Facebook
          </a>
        </div>

        <p className="text-center text-xs text-gray-500">
          You don’t need a separate Work Pool registration when signing in with social providers.
        </p>

        <p className="text-center text-sm text-gray-600">
          New here?{' '}
          <Link to="/register" className="text-blue-600 font-medium hover:underline">See social sign-up options</Link>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
