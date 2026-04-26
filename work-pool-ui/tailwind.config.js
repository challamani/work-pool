/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#f5f3ff',
          100: '#ede9fe',
          200: '#ddd6fe',
          300: '#c4b5fd',
          400: '#a78bfa',
          500: '#8b5cf6',
          600: '#7c3aed',
          700: '#6d28d9',
          800: '#5b21b6',
          900: '#4c1d95',
          950: '#2e1065',
        },
        ocean: {
          400: '#22d3ee',
          500: '#06b6d4',
          600: '#0891b2',
        },
        flame: {
          400: '#fb923c',
          500: '#f97316',
          600: '#ea580c',
        },
      },
      backgroundImage: {
        'gradient-brand':  'linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%)',
        'gradient-brand-r':'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)',
        'gradient-hero':   'linear-gradient(135deg, #f5f3ff 0%, #ede9fe 40%, #e0f2fe 100%)',
        'gradient-card':   'linear-gradient(135deg, #faf5ff 0%, #f0f9ff 100%)',
      },
      animation: {
        'spin-slow':   'spin 3s linear infinite',
        'fade-in':     'fadeIn 0.5s ease-in-out',
        'slide-up':    'slideUp 0.4s ease-out',
        'float':       'float 6s ease-in-out infinite',
        'pulse-brand': 'pulseBrand 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
      keyframes: {
        fadeIn:  { '0%': { opacity: '0' },                                         '100%': { opacity: '1' } },
        slideUp: { '0%': { opacity: '0', transform: 'translateY(20px)' },           '100%': { opacity: '1', transform: 'translateY(0)' } },
        float:   { '0%,100%': { transform: 'translateY(0px)' },                    '50%': { transform: 'translateY(-8px)' } },
        pulseBrand: {
          '0%,100%': { boxShadow: '0 0 0 0 rgba(124,58,237,0.4)' },
          '50%':     { boxShadow: '0 0 0 8px rgba(124,58,237,0)' },
        },
      },
      boxShadow: {
        'brand':       '0 4px 24px -4px rgba(124,58,237,0.30)',
        'brand-lg':    '0 8px 32px -4px rgba(124,58,237,0.35)',
        'card':        '0 1px 3px 0 rgba(0,0,0,0.05), 0 4px 16px -4px rgba(0,0,0,0.08)',
        'card-hover':  '0 8px 32px -4px rgba(124,58,237,0.18)',
        'glass':       '0 8px 32px 0 rgba(100,80,200,0.10)',
      },
      backdropBlur: {
        xs: '2px',
      },
    },
  },
  plugins: [],
}

