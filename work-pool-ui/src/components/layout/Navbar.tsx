import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Bell, Briefcase, User, LogOut, PlusCircle, Home, Wallet } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { useNotificationStore } from '../../store/notificationStore';

const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuthStore();
  const { unreadCount } = useNotificationStore();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => location.pathname === path;

  const navLink = (path: string, label: string, Icon: React.ElementType) => (
    <Link
      to={path}
      className={`flex items-center gap-1.5 text-sm font-medium px-3 py-1.5 rounded-full transition-all duration-200
        ${isActive(path)
          ? 'bg-brand-100 text-brand-700 shadow-sm'
          : 'text-slate-600 hover:text-brand-600 hover:bg-brand-50'}`}
    >
      <Icon className="w-4 h-4" />
      {label}
    </Link>
  );

  return (
    <nav className="bg-white/70 border-b border-white/50 sticky top-0 z-50 backdrop-blur-md shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">

          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 group">
            <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-brand-600 to-indigo-600 flex items-center justify-center shadow-brand group-hover:scale-110 transition-transform">
              <Briefcase className="w-4 h-4 text-white" />
            </div>
            <span className="text-lg font-bold bg-clip-text text-transparent bg-gradient-to-r from-brand-700 to-indigo-600">
              Work Pool
            </span>
            <span className="hidden sm:inline text-[10px] font-semibold bg-gradient-to-r from-brand-100 to-indigo-100 text-brand-700 px-2 py-0.5 rounded-full border border-brand-200">
              India
            </span>
          </Link>

          {/* Desktop nav */}
          <div className="hidden md:flex items-center gap-1">
            {navLink('/', 'Home', Home)}
            {navLink('/tasks', 'Browse Tasks', Briefcase)}
            {isAuthenticated && navLink('/tasks/new', 'Post Task', PlusCircle)}
          </div>

          {/* Right side */}
          <div className="flex items-center gap-2">
            {isAuthenticated ? (
              <>
                {/* Wallet */}
                <Link to="/wallet"
                  className="hidden sm:flex items-center gap-1 text-xs font-medium text-slate-600 hover:text-brand-600 px-2 py-1.5 rounded-full hover:bg-brand-50 transition-colors">
                  <Wallet className="w-4 h-4" />
                  <span className="hidden lg:inline">Wallet</span>
                </Link>

                {/* Notifications */}
                <Link to="/notifications" className="relative p-2 rounded-full hover:bg-brand-50 text-slate-600 hover:text-brand-600 transition-colors">
                  <Bell className="w-5 h-5" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 w-4 h-4 bg-gradient-to-br from-flame-500 to-red-500 text-white text-[9px] font-bold rounded-full flex items-center justify-center animate-pulse-brand">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </Link>

                {/* Avatar */}
                <Link to="/profile" className="flex items-center gap-2 group">
                  {user?.profileImageUrl ? (
                    <img src={user.profileImageUrl} alt={user.fullName}
                      className="w-8 h-8 rounded-full object-cover ring-2 ring-brand-200 group-hover:ring-brand-400 transition-all" />
                  ) : (
                    <div className="w-8 h-8 rounded-full bg-gradient-to-br from-brand-400 to-indigo-500 flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform">
                      <User className="w-4 h-4 text-white" />
                    </div>
                  )}
                  <span className="hidden md:block text-sm font-medium text-slate-700 max-w-24 truncate group-hover:text-brand-700 transition-colors">
                    {user?.fullName?.split(' ')[0]}
                  </span>
                </Link>

                <button onClick={handleLogout}
                  className="p-2 rounded-full text-slate-400 hover:text-red-500 hover:bg-red-50 transition-colors" title="Logout">
                  <LogOut className="w-4 h-4" />
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn-secondary text-sm px-4 py-2">Login</Link>
                <Link to="/register" className="btn-primary text-sm px-4 py-2">Join Free ✨</Link>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Mobile bottom nav */}
      {isAuthenticated && (
        <div className="md:hidden fixed bottom-0 left-0 right-0 z-50
          bg-white/80 backdrop-blur-md border-t border-white/60 shadow-[0_-4px_24px_rgba(100,80,200,0.12)]
          flex safe-area-inset-bottom">
          {[
            { to: '/',              Icon: Home,       label: 'Home'    },
            { to: '/tasks',         Icon: Briefcase,  label: 'Tasks'   },
            { to: '/tasks/new',     Icon: PlusCircle, label: 'Post'    },
            { to: '/notifications', Icon: Bell,       label: 'Alerts', badge: unreadCount },
            { to: '/profile',       Icon: User,       label: 'Profile' },
          ].map(({ to, Icon, label, badge }) => (
            <Link key={to} to={to}
              className={`flex-1 flex flex-col items-center py-2.5 text-[10px] font-semibold transition-colors
                ${isActive(to) ? 'text-brand-600' : 'text-slate-500'}`}>
              <div className={`relative p-1 rounded-xl ${isActive(to) ? 'bg-brand-100' : ''}`}>
                <Icon className="w-5 h-5" />
                {badge && badge > 0 && (
                  <span className="absolute -top-0.5 -right-0.5 w-3.5 h-3.5 bg-flame-500 text-white text-[8px] font-bold rounded-full flex items-center justify-center">
                    {badge > 9 ? '9+' : badge}
                  </span>
                )}
              </div>
              {label}
            </Link>
          ))}
        </div>
      )}
    </nav>
  );
};

export default Navbar;
