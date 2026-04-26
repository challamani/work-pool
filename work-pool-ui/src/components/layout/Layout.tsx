import React from 'react';
import Navbar from './Navbar';
import { Outlet } from 'react-router-dom';

const Layout: React.FC = () => (
  <div className="min-h-screen relative">
    {/* Decorative background blobs */}
    <div className="blob blob-violet w-96 h-96 top-[-80px] left-[-100px] fixed" />
    <div className="blob blob-cyan   w-80 h-80 top-[40%]  right-[-80px] fixed" />
    <div className="blob blob-amber  w-64 h-64 bottom-[-40px] left-[30%] fixed" />
    <Navbar />
    <main className="relative z-10 pb-20 md:pb-4">
      <Outlet />
    </main>
  </div>
);

export default Layout;
