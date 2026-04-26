import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Briefcase, Sparkles } from 'lucide-react';

const LoginPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const hasOauthError = !!searchParams.get('error');

  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

  return (
    <div className="min-h-[85vh] flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-sm">

        {/* Brand header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-br from-brand-600 to-indigo-600 shadow-brand-lg mb-4">
            <Briefcase className="w-7 h-7 text-white" />
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 leading-tight">Welcome back</h1>
          <p className="text-slate-500 text-sm mt-1.5">Sign in to continue to Work Pool India</p>
        </div>

        <div className="card p-7 space-y-5">
          {hasOauthError && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl flex items-center gap-2">
              <span className="text-base">&#9888;&#65039;</span>
              Social login failed. Please try again.
            </div>
          )}

          <div className="space-y-3">
            <a href={`${apiBase}/api/v1/auth/oauth2/authorization/google`}
              className="flex items-center justify-center gap-3 w-full px-4 py-3 rounded-xl border border-slate-200 bg-white font-semibold text-slate-700 text-sm
                         hover:border-brand-300 hover:bg-brand-50 hover:text-brand-700 transition-all duration-200 shadow-sm hover:shadow-brand">
              <img src="https://www.google.com/favicon.ico" alt="Google" className="w-4 h-4" />
              Continue with Google
            </a>
            <a href={`${apiBase}/api/v1/auth/oauth2/authorization/facebook`}
              className="flex items-center justify-center gap-3 w-full px-4 py-3 rounded-xl border border-slate-200 bg-white font-semibold text-slate-700 text-sm
                         hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 shadow-sm">
              <span className="w-4 h-4 flex items-center justify-center font-black text-blue-600 text-base leading-none">f</span>
              Continue with Facebook
            </a>
          </div>

          <div className="flex items-center gap-3 text-xs text-slate-300">
            <div className="flex-1 h-px bg-slate-100" />
            Secure &amp; instant
            <div className="flex-1 h-px bg-slate-100" />
          </div>

          <p className="text-center text-xs text-slate-400 leading-relaxed">
            No separate Work Pool account needed — your social profile is all you need.
          </p>
        </div>

        <p className="text-center text-sm text-slate-500 mt-6">
          New here?{' '}
          <Link to="/register" className="text-brand-600 font-semibold hover:text-brand-700 inline-flex items-center gap-1">
            Create a free account <Sparkles className="w-3.5 h-3.5" />
          </Link>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
