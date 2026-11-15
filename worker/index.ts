// Cloudflare Worker: static site from ./dist, plus a proxy that forwards /api/* to the
// backend so the browser only ever talks to one origin (no CORS, CSP stays 'self').
// Only /api/* reaches this code (see run_worker_first in wrangler.jsonc).
// The backend address comes from the PUBLIC_API_BASE_URL variable (Cloudflare dashboard >
// the Worker > Settings > Variables and secrets), read per request, so changing it needs
// no code change or frontend rebuild.
interface Env {
  PUBLIC_API_BASE_URL?: string;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const base = env.PUBLIC_API_BASE_URL?.trim().replace(/\/+$/, "");
    if (!base || !base.startsWith("https://")) {
      return new Response(
        JSON.stringify({ status: 500, detail: "API is not configured" }),
        { status: 500, headers: { "Content-Type": "application/json" } }
      );
    }

    const url = new URL(request.url);
    return fetch(new Request(base + url.pathname + url.search, request));
  },
};
