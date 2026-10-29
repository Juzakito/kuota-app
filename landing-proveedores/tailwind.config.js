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
          crayola: '#2F7AEF',  // Crayola Blue — Textos principales y acentos
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
      },
      animation: {
        marquee: 'marquee 18s linear infinite',
      },
    },
  },
  plugins: [],
};
