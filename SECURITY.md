# Security

No app is "unhackable". This file says what is protected, how it was checked, and
what is still open.

## What is protected

| Risk | Protection | Where |
| --- | --- | --- |
| Browser changing prices | Server prices every order from the database; any price sent by the browser is ignored | `OrderService` |
| Guessing an order's `requestId` to read someone's name, phone and address | `requestId` must be a UUID (unguessable) | `OrderDtos` |
| Brute-forcing the admin key | 10 wrong keys from one IP locks admin calls out for 15 minutes, even for the right key | `RequestLimitFilter` |
| Order spam / email spam | 10 orders per hour per IP | `RequestLimitFilter` |
| Huge request bodies | Bodies over 20 KB get `413` | `RequestLimitFilter` |
| Clickjacking, MIME sniffing, referrer leaks | `X-Frame-Options`, `nosniff`, `Referrer-Policy`, API-only CSP, Permissions-Policy, HSTS over HTTPS | `SecurityHeadersFilter` |
| Admin / order responses cached | `Cache-Control: no-store` on `/api/admin` and `/api/orders` | `SecurityHeadersFilter` |
| Error pages leaking internals | No stack traces, paths, timestamps or exception names; no `Server` header | `application.properties`, `MinimalErrorAttributes` |
| Admin path tricks (`//`, `;x=1`, `%61dmin`, `..`) | Never return data (tested) | `SecurityTest` |
| Weak admin key in production | With `SPRING_PROFILES_ACTIVE=prod` the app refuses to start if the key is under 24 characters | `ProductionSecurityCheck` |
| Script injection (XSS) | React escapes output; no `dangerouslySetInnerHTML` or `eval`; production CSP allows only the site's own scripts and styles | `vite.config.ts` |
| Source code visible in dev tools | Production build has no source maps and is minified | `vite.config.ts` |
| Secrets in git or in the bundle | Secrets only in git-ignored `backend/.env.local` / environment; the built bundle was searched for keys and passwords (none) | |

## What dev tools show

Only what the storefront needs: the public catalogue, minimum order value, delivery
quote and the customer's own order. Not shown: other customers' data, prices the
server would accept from the browser, stack traces, framework versions, or keys.
The admin key sits in `sessionStorage` after login (cleared when the tab closes).

## Production checklist

1. `SPRING_PROFILES_ACTIVE=prod`.
2. `ADMIN_API_KEY` from `openssl rand -hex 32`. Never reuse `local-test-key`.
3. Serve everything over HTTPS (HSTS and `crypto.randomUUID` need it).
4. Behind a reverse proxy, set `server.forward-headers-strategy=native` (or
   `framework`) and make the proxy overwrite `X-Forwarded-For`. Otherwise every
   visitor shares the proxy's IP and one person can lock out admin for all.
5. Serve the built `dist/` with the same headers (nosniff, frame deny, HSTS) and
   proxy `/api` to the backend.
6. Use a strong, unique `DB_PASSWORD`; the database must not be reachable from the internet.
7. **Revoke the Gmail App Password that was pasted in chat** at
   https://myaccount.google.com/apppasswords, create a new one and put it in `.env.local`.
8. Run `npm audit` and an OWASP dependency check before launch.

## Known limits (not fixed)

- **One shared admin key**, no real accounts, roles or audit log.
- **Rate limits are in memory:** they reset on restart and are per server instance.
- **No CAPTCHA:** a bot using many IPs can still place fake orders.
- **No payments yet:** when added, mark orders paid only from a verified gateway webhook.
- **No WAF / DDoS protection:** use a CDN or cloud provider in front.
- `GET /error` called directly returns status 500 with a body that reveals nothing.

## How it was checked

- `./mvnw test`: `SecurityTest`, `RateLimitTest` and `ProductionSecurityCheckTest`.
- Live probe against the running backend: malformed JSON `400`, weak `requestId` `400`,
  100 KB body `413`, admin lockout `429` after 10 wrong keys, order cap `429` after 10 posts.
- Full checkout in headless Chrome on the production build with the CSP active, with no violations.
