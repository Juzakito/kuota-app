/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        forest: {
          DEFAULT: '#163300', // Forest Ink — Dominant brand dark
          dark: '#0E1F00',
        },
        lime: {
          DEFAULT: '#9FE870', // Lime Voltage — Functional punctuation, CTAs, accents
          hover: '#8CD85E',
        },
        spruce: '#054D28',    // Spruce — Secondary dark green for cards & depth
        linen: '#E2F6D5',     // Linen Mist — Pale green wash for highlight surfaces
        fog: '#E8EBE6',       // Fog — Card surfaces, soft gray
        paper: '#FFFFFF',     // Paper — Canvas, pure white
        charcoal: '#454745',  // Charcoal — Primary text, soft warm black
        obsidian: '#0E0F0C',  // Obsidian — Display headlines, green-tinted black
        pebble: '#868685',    // Pebble — Muted text, borders
        slate: '#6A6C6A',     // Slate — Helper labels
        alarm: '#CB272F',     // Alarm Red — Accent
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
      borderRadius: {
        pill: '9999px',
        card: '10px',
        hero: '28px',
      },
      boxShadow: {
        subtle: 'rgba(14, 15, 12, 0.08) 0px 1px 3px 0px',
        card: '0 4px 20px -2px rgba(22, 51, 0, 0.06)',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        marquee: {
          '0%': { transform: 'translateX(0%)' },
          '100%': { transform: 'translateX(-50%)' },
        },
      },
      animation: {
        fadeIn: 'fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards',
        marquee: 'marquee 25s linear infinite',
      },
    },
  },
  plugins: [],
};
