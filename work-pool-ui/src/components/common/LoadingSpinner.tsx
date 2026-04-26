import React from 'react';

interface LoadingSpinnerProps {
  className?: string;
  size?: 'sm' | 'md' | 'lg';
}

const sizeClass = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12' };

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ className = '', size = 'md' }) => (
  <div className={`flex justify-center items-center ${className}`}>
    <div
      className={`${sizeClass[size]} rounded-full animate-spin`}
      style={{
        background: 'conic-gradient(from 0deg, transparent 0%, #7c3aed 100%)',
        WebkitMask: 'radial-gradient(farthest-side, transparent calc(100% - 2px), white calc(100% - 2px))',
        mask: 'radial-gradient(farthest-side, transparent calc(100% - 2px), white calc(100% - 2px))',
      }}
    />
  </div>
);

export default LoadingSpinner;
