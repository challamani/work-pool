import React from 'react';
import Navbar from './Navbar';
import { Outlet } from 'react-router-dom';

const Layout: React.FC = () => (
  <div className="min-h-screen bg-gray-50">
    <Navbar />
    <main className="pb-16 md:pb-0">
      <Outlet />
    </main>
  </div>
);

export default Layout;
