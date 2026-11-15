// Cloudflare Pages Function: forwards /api/* to the backend so the browser only ever
// talks to one origin (no CORS, and the page CSP stays 'self').
// The backend address comes from the PUBLIC_API_BASE_URL environment variable
// (Cloudflare dashboard > Pages project > Settings > Variables), read at request time,
// so changing it needs no code change or frontend rebuild.
interface Env {
  PUBLIC_API_BASE_URL?: string;
}

export async function onRequest({
  request,
  env,
}: {
  request: Request;
  env: Env;
}) {
  const base = env.PUBLIC_API_BASE_URL?.trim().replace(/\/+$/, "");
  if (!base || !base.startsWith("https://")) {
    return new Response(
      JSON.stringify({ status: 500, detail: "API is not configured" }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }

  const url = new URL(request.url);
  return fetch(new Request(base + url.pathname + url.search, request));
}
