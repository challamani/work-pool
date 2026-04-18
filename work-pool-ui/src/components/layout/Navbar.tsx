import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Bell, Briefcase, User, LogOut, PlusCircle, Home } from 'lucide-react';
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

  const isActive = (path: string) =>
    location.pathname === path ? 'text-blue-600 font-semibold' : 'text-gray-600 hover:text-blue-600';

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2">
            <Briefcase className="w-7 h-7 text-blue-600" />
            <span className="text-xl font-bold text-gray-900">Work Pool</span>
            <span className="hidden sm:inline text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">India</span>
          </Link>

          {/* Desktop nav links */}
          <div className="hidden md:flex items-center gap-6">
            <Link to="/" className={`flex items-center gap-1 text-sm ${isActive('/')}`}>
              <Home className="w-4 h-4" /> Home
            </Link>
            <Link to="/tasks" className={`flex items-center gap-1 text-sm ${isActive('/tasks')}`}>
              <Briefcase className="w-4 h-4" /> Browse Tasks
            </Link>
            {isAuthenticated && (
              <Link to="/tasks/new" className={`flex items-center gap-1 text-sm ${isActive('/tasks/new')}`}>
                <PlusCircle className="w-4 h-4" /> Post Task
              </Link>
            )}
          </div>

          {/* Right side */}
          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <>
                <Link to="/notifications" className="relative p-2 text-gray-600 hover:text-blue-600">
                  <Bell className="w-5 h-5" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </Link>
                <Link to="/profile" className="flex items-center gap-2">
                  {user?.profileImageUrl ? (
                    <img src={user.profileImageUrl} alt={user.fullName} className="w-8 h-8 rounded-full object-cover" />
                  ) : (
                    <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                      <User className="w-4 h-4 text-blue-600" />
                    </div>
                  )}
                  <span className="hidden md:block text-sm font-medium text-gray-700 max-w-24 truncate">
                    {user?.fullName?.split(' ')[0]}
                  </span>
                </Link>
                <button onClick={handleLogout} className="p-2 text-gray-500 hover:text-red-600" title="Logout">
                  <LogOut className="w-4 h-4" />
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn-secondary text-sm px-3 py-1.5">Login</Link>
                <Link to="/register" className="btn-primary text-sm px-3 py-1.5">Join Free</Link>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Mobile bottom nav */}
      {isAuthenticated && (
        <div className="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 flex z-50">
          <Link to="/" className="flex-1 flex flex-col items-center py-2 text-xs text-gray-600">
            <Home className="w-5 h-5" />Home
          </Link>
          <Link to="/tasks" className="flex-1 flex flex-col items-center py-2 text-xs text-gray-600">
            <Briefcase className="w-5 h-5" />Tasks
          </Link>
          <Link to="/tasks/new" className="flex-1 flex flex-col items-center py-2 text-xs text-blue-600">
            <PlusCircle className="w-5 h-5" />Post
          </Link>
          <Link to="/notifications" className="relative flex-1 flex flex-col items-center py-2 text-xs text-gray-600">
            <Bell className="w-5 h-5" />
            {unreadCount > 0 && (
              <span className="absolute top-1 right-5 w-3.5 h-3.5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center text-[9px]">{unreadCount}</span>
            )}
            Alerts
          </Link>
          <Link to="/profile" className="flex-1 flex flex-col items-center py-2 text-xs text-gray-600">
            <User className="w-5 h-5" />Profile
          </Link>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
