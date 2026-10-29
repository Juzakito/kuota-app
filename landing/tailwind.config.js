/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        kuota: {
          dodger: '#0391FD',   // Dodger Blue — CTAs y elementos destacados
          crayola: '#2F7AEF',  // Crayola Blue — Textos y acentos secundarios
          palesky: '#CBE5FE',  // Pale Sky — Fondo principal (tema único light)
          deepnavy: '#00235C', // Deep Navy — Bordes, líneas, titulares y alto contraste
        },
      },
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'system-ui', '-apple-system', 'sans-serif'],
      },
      boxShadow: {
        'navy-hard': '4px 4px 0px 0px #00235C',
        'navy-hard-lg': '6px 6px 0px 0px #00235C',
        'dodger-glow': '0 10px 30px -5px rgba(3, 145, 253, 0.45)',
      },
      keyframes: {
        marquee: {
          '0%': { transform: 'translateX(0%)' },
          '100%': { transform: 'translateX(-50%)' },
        },
        fadeIn: {
          '0%': { opacity: '1', transform: 'translateY(4px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        digitSlide: {
          '0%': { opacity: '0.45', transform: 'translateY(-4px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        floatCard: {
          '0%, 100%': { transform: 'translate3d(0, 0px, 0)' },
          '50%': { transform: 'translate3d(0, -6px, 0)' },
        },
      },
      animation: {
        marquee: 'marquee 18s linear infinite',
        fadeIn: 'fadeIn 0.35s ease-out forwards',
        digitSlide: 'digitSlide 0.22s cubic-bezier(0.16, 1, 0.3, 1) forwards',
        floatCard: 'floatCard 5s ease-in-out infinite',
      },
    },
  },
  plugins: [],
};
