import React from 'react';

interface LoadingSpinnerProps {
  className?: string;
  size?: 'sm' | 'md' | 'lg';
}

const sizeClass = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12' };

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ className = '', size = 'md' }) => (
  <div className={`flex justify-center items-center ${className}`}>
    <div className={`${sizeClass[size]} border-2 border-blue-200 border-t-blue-600 rounded-full animate-spin`} />
  </div>
);

export default LoadingSpinner;
