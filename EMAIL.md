# Order confirmation emails

When a customer places an order, the backend sends a
confirmation email through **Gmail SMTP**. There is no extra email service to pay for.

## What is sent
- **When:** right after a **new** order is saved. A retried checkout (same `requestId`)
  does not send a second email.
- **To:** the email the customer typed at checkout (required, so every order gets one).
- **Subject:** `Order Confirmation - AG-1003`
- **Body:** order number, the box contents, products / packaging / delivery / total,
  the delivery address, and a note that payment is pending.
- **Safety:** the email is sent after the order is committed and off the request
  thread. If Gmail fails or is not configured, the order is still saved and the
  customer still sees the confirmation page. The failure is only written to the backend log.

## Set it up
1. Turn on **2-Step Verification** for the Gmail account:
   https://myaccount.google.com/security
2. Create a **Google App Password**: https://myaccount.google.com/apppasswords
   (name it e.g. "Affordable Gifts"). Google shows a 16-character password once.
   Use this, **never your normal Gmail password**.
3. Add both values to `backend/.env.local` (the file is git-ignored):
   ```
   GMAIL_USERNAME=your-address@gmail.com
   GMAIL_APP_PASSWORD=xxxxxxxxxxxxxxxx
   ```
4. Load the file and start the backend:
   ```bash
   cd backend
   set -a; . ./.env.local; set +a
   ./mvnw spring-boot:run
   ```
   Google shows the App Password in groups of four with spaces. Remove the spaces.
   Do not put quotes around the values.
5. Place a test order with your own email address and check the inbox (and spam).
   A quick way, without the UI (change `you@example.com` to your address):
   ```bash
   curl -s -X POST localhost:8080/api/orders -H 'Content-Type: application/json' -d '{"requestId":"3f2b8c1e-6d4a-4b7e-9a10-5c2d8e7f1a01","packagingId":"box-2","quantity":2,"productIds":["almonds","cashews"],"customer":{"name":"Mail Test","phone":"9876543210","email":"you@example.com"},"address":{"house":"1","street":"MG Road","area":"Indiranagar","city":"Bengaluru","state":"Karnataka","pincode":"560001","instructions":""}}'
   ```
   Use a new `requestId` each time. Then watch the backend log for
   `Sent confirmation email for AG-...`. If you see `Could not send confirmation email`,
   the reason follows it (usually a wrong App Password or 2-Step Verification being off).
   Delete the test order afterwards from the admin console or with `psql`.

With `GMAIL_USERNAME` unset, the backend logs
`Email not configured (GMAIL_USERNAME unset); skipping confirmation for AG-...`
and carries on.

## SMTP settings (already in `application.properties`)
| Setting | Value |
|---|---|
| Host / port | `smtp.gmail.com` / `587` |
| Encryption | STARTTLS |
| Authentication | yes |
| Username | `GMAIL_USERNAME` |
| Password | `GMAIL_APP_PASSWORD` |
| Timeouts | 5 seconds, so a slow Gmail cannot hang the backend |

## Limits and caveats
- A personal Gmail account allows roughly **500 emails per day**. Google can change
  this. That is fine for the MVP; move to a transactional email service when volume grows.
- Gmail rewrites the sender to the account's own address, so replies go to that Gmail.
  A business address (for example `orders@yourdomain`) would look more professional later.
- Emails are plain text for now.
- If sending fails, there is **no retry**. Check the backend log for
  `Could not send confirmation email for AG-...`.
- Not covered yet: status-change emails (packed, dispatched, delivered) and an email to
  the shop owner for each new order.

## Tests
`OrderEmailTest` uses a mock mail sender, so running `./mvnw test` never sends a real email.
It checks the recipient and content, that an order without an email is refused and nothing is sent, that a
mail failure does not break the order, and that a retried checkout sends only one email.
