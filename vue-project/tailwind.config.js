/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#0EA5E9',
          hover: '#0284C7',
        },
        secondary: '#06B6D4',
        accent: '#10B981',
      },
      fontFamily: {
        sans: ['Be Vietnam Pro', 'Noto Sans SC', 'system-ui', 'sans-serif'],
      },
      animation: {
        'gradient': 'gradientBG 20s ease infinite',
        'float': 'float 20s ease-in-out infinite',
        'progress': 'progress 2s linear forwards',
      },
      keyframes: {
        gradientBG: {
          '0%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
          '100%': { backgroundPosition: '0% 50%' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0) rotate(0deg)' },
          '50%': { transform: 'translateY(-20px) rotate(5deg)' },
        },
        progress: {
          'from': { width: '0%' },
          'to': { width: '100%' },
        },
      },
    },
  },
  plugins: [],
}
