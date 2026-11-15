import react from '@vitejs/plugin-react'
import { defineConfig, type Plugin } from 'vite'

// Production pages may only load their own scripts/styles and talk to their own origin.
// Added at build time only: the dev server needs inline scripts for hot reload.
const csp: Plugin = {
  name: 'content-security-policy',
  apply: 'build',
  transformIndexHtml: () => [
    {
      tag: 'meta',
      attrs: {
        'http-equiv': 'Content-Security-Policy',
        content:
          "default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self' data:; " +
          "connect-src 'self'; base-uri 'none'; form-action 'none'",
      },
      injectTo: 'head-prepend',
    },
    {
      tag: 'meta',
      attrs: { name: 'referrer', content: 'no-referrer' },
      injectTo: 'head-prepend',
    },
  ],
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), csp],
  build: {
    sourcemap: false, // never ship readable source to the browser
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
