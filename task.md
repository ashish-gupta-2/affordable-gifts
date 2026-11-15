# Affordable Gifts – Task Tracker

Move an item from **Pending** to **Done** (`[ ]` → `[x]`) when it is finished and
`npm run build`, `npm run lint` and `./mvnw test` (in `backend/`) pass.

## item-done

### Frontend – gift box builder
- [x] Tailwind v4 + PostCSS setup, template CSS removed
- [x] Responsive layout with sticky cart column
- [x] Packaging selector (Gift Box, Glass Jars)
- [x] Slot-based builder: one product per partition/jar, pack size = slot size
- [x] Product cards show pack price for the selected slot size
- [x] Box quantity −/+ (identical copies; price, weight and minimum scale with it)
- [x] Minimum order progress message and live totals
- [x] Catalogue loaded from the backend (`src/api/catalog.ts`, Vite `/api` proxy)
- [x] Minimum order value loaded from backend settings

### Frontend – checkout
- [x] Build → Checkout → Confirmation flow (`App.tsx`)
- [x] Checkout form: name, mobile, email, address, PIN code (all required, marked with a red *), delivery instructions (optional)
- [x] Live delivery quote by PIN code (local ₹50 / India-wide ₹110)
- [x] Order summary with products, packaging, delivery and total
- [x] Form and box kept when going "Back to edit your box"
- [x] Order confirmation page with order number
- [x] Checkout button disabled until box is full and minimum is met

### Admin console (`/admin`)
- [x] Admin login with the admin key (kept in sessionStorage, 401 signs out)
- [x] Orders list with status filter; order detail with customer, address and items
- [x] Update order status through the shipping stages; mark paid / unpaid
- [x] Delivered and cancelled orders are locked from further status changes
- [x] Products: add, edit pack prices (100g / 200g), show / hide
- [x] Packaging: add, edit, show / hide (jar sets validated as 2×200g or 3×100g)
- [x] Settings: minimum order value and delivery charges (storefront uses them at once)
- [x] Backend: `GET/PATCH /api/admin/orders`, `GET/PUT /api/admin/settings`

### Backend (`backend/`: Spring Boot 4.1, Java 25, Postgres, Flyway)
- [x] Project scaffold, schema and dev seed data
- [x] Public API: `GET /api/products`, `GET /api/packaging`, `GET /api/settings`
- [x] Health check `GET /api/health` returns `{"status":"UP"}` (for Render's health check path)
- [x] Admin API (`X-Admin-Key`): create/read/update/soft-delete products and packaging
- [x] Validation and error mapping (400 / 401 / 404 / 409)
- [x] `GET /api/delivery-quote?pincode=`
- [x] `POST /api/orders`: server-side pricing, price snapshot, never trusts browser prices
- [x] Idempotent checkout via `requestId` (retry returns the same order)
- [x] Order numbers (`AG-1001`…), items stored per slot
- [x] Order confirmation email via Gmail SMTP (sent after commit, never breaks an order; see `EMAIL.md`)
- [x] Tests: 61 passing (catalogue, orders, admin console, email, security, images, health)
- [x] Lombok annotation processor declared in `pom.xml` (needed on JDK 23+; a clean build had been failing)

### Product and packaging photos (see `IMAGES.md`)
- [x] Upload a photo for each product, gift box and glass jar set from the admin console
- [x] Photos stored in Postgres (`images` table) and served at `/api/images/{id}`, cached for a year
- [x] Only real JPEG / PNG / WebP up to 1 MB (SVG refused); photo URLs limited to our own uploads
- [x] Storefront cards, packaging cards and cart show the photo; the emoji is now an optional fallback

### Glass jar covers
- [x] New packaging type "Glass jar cover": its own priced item in two standard layouts (3 x 100g or 2 x 200g)
- [x] Storefront shows a "Glass Jars Cover" group (red *) only after jars are chosen, listing only covers that fit those jars
- [x] Several covers per layout are allowed; the customer picks one. Checkout stays disabled until a cover is chosen
- [x] A jar set costs (price per jar x number of jars) + the chosen cover; the order stores the combined price and the name "Jar + Cover"
- [x] The server re-checks the cover (active, right layout, only for jars); covers cannot be ordered alone
- [x] Admin shows a "Fits" picker for jars and covers instead of free numbers
- [x] Checked in headless Chrome: 3 x Rs 20 + Rs 20 = Rs 80, with the Gold cover Rs 95; order placed and shown as "Jar + Cover"
- [x] Migration V4 turned existing jar prices into per-jar prices and added two covers at Rs 0 (set real cover prices in /admin)

### Security (see `SECURITY.md`)
- [x] Security headers, `no-store` on admin/order responses, no server banner
- [x] Error responses reveal no path, timestamp, stack trace or exception
- [x] `requestId` must be a UUID (stops replay / reading someone else's order)
- [x] Admin brute-force lockout, per-IP order cap, 20 KB body limit
- [x] Production refuses to start with an admin key under 24 characters
- [x] Frontend: production CSP, no source maps, no secrets in the bundle

### Database and docs
- [x] Local Postgres databases `affordable_gifts` (dev) and `affordable_gifts_test`
- [x] Password login enforced for the project databases (role `affordable_gifts_app`)
- [x] Credentials kept in git-ignored `backend/.env.local`
- [x] `DB.md`: quick start, connecting, tables, Flyway, reset steps

## item-pending

### Photos follow-ups
- [ ] Move photos to Cloudflare R2 when the catalogue grows (only `ImageService` changes; see `IMAGES.md`)
- [ ] Delete unused photos left behind when one is replaced

### Security follow-ups
- [ ] Revoke the exposed Gmail App Password and create a new one
- [ ] Real admin accounts with login, roles and an audit log (replace the shared key)
- [ ] CAPTCHA on checkout
- [ ] HTTPS, reverse proxy forwarded-header setup, WAF / CDN (see production checklist)
- [ ] Dependency vulnerability scans (`npm audit`, OWASP dependency-check)

### Next up – Admin console follow-ups
- [ ] Real admin accounts/roles (the console still uses the shared `X-Admin-Key`)
- [ ] Order notes and tracking number / courier fields on an order
- [ ] Search orders by number, name or phone; date filter; pagination
- [ ] Product purchase cost and margin (PRD 6.5)
- [ ] Audit log of who changed what

### Payments (ON HOLD – to be done later)
Orders are saved with payment `PENDING`, and an admin marks them paid by hand.
To restart this, we need: the chosen gateway (Razorpay was suggested in the PRD),
an account, and test API keys.
- [ ] Payment gateway (e.g. Razorpay): create payment order on backend
- [ ] Verify payment signature / webhook; never mark paid from the browser alone
- [ ] Handle payment failure and pending payments
- [ ] Store gateway reference ID; move order to "Payment confirmed"

### Storefront features
- [ ] Greeting card: choose design, personal message, preview, store with order
- [ ] Set up Gmail App Password in `backend/.env.local` and send a real test email (see `EMAIL.md`)
- [ ] More notifications: status-change emails, email to the shop owner, SMS / WhatsApp
- [ ] Order tracking page for customers
- [ ] "Suggest a bigger box" nudge
- [ ] Mixed orders (several different boxes/jar sets in one cart)

### Delivery and tax
- [ ] Real PIN code serviceability check
- [ ] Delivery charge by packed weight / courier rates
- [ ] Tax (GST) treatment decided and applied
- [ ] Discounts / promo codes

### Business decisions needed
- [ ] Real product and packaging prices (seed prices are placeholders)
- [ ] Decide whether the ₹299 minimum includes packaging (PRD says product + packaging; code counts products only)
- [ ] Confirm real delivery rates (₹50 / ₹110 are PRD demo values)

### Quality and release
- [ ] Real admin authentication (replace the shared `X-Admin-Key`)
- [ ] Production config: `SPRING_PROFILES_ACTIVE=prod`, `ADMIN_API_KEY`, DB credentials
- [ ] Frontend tests (admin console and checkout were only checked by hand in headless Chrome)
- [ ] Commit work (nothing is committed yet on `feature/frontend-mvp`)
- [ ] Remove unused `src/data/cards.ts` or use it for greeting cards
