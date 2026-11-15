# Product and packaging photos

## How to add a photo
1. Open `/admin`, then **Products** or **Packaging**.
2. **Add** or **Edit** an item, and choose a file under **Photo**. It uploads at once and
   shows a preview. **Remove photo** puts the emoji back.
3. Click **Save**. The storefront shows the photo straight away.

Rules: JPG, PNG or WebP, up to **1 MB**. Square photos (for example 800 x 800) look best,
because the cards crop to fit. Shrink big camera photos first (squoosh.app is free).
A product's emoji is only a fallback now, and it may be left empty.

## Where the files are stored
**Today: in the Postgres database** (table `images`, column `data`). The photo's address,
`/api/images/<id>`, is saved on the product or packaging row.

Why the database, for now:
- Render's disk is wiped on every deploy, so files on disk would disappear.
- Photos are backed up and restored with everything else.
- No extra account, key or cost, and nothing else to configure.

Limits of this choice:
- Neon's free database is 0.5 GB. At about 300 KB per photo that is well over a thousand
  photos, plenty for a catalogue of dozens.
- Every photo request goes through the backend, so the first one after Render's free tier
  sleeps is slow. Photos are sent with `Cache-Control: immutable` for a year, so browsers
  and Cloudflare keep them after that.
- Replaced or removed photos stay in the table (about 300 KB each). Not worth cleaning up
  until there are hundreds.

## Best long-term option: Cloudflare R2
When the catalogue grows, or you want photos independent of the backend:
- **R2** is S3-compatible object storage with **no download fees**; the free tier is
  10 GB. Photos are served from Cloudflare's network.
- You already use Cloudflare, so there is no new vendor.
- Only [`ImageService`](backend/src/main/java/com/affordablegifts/backend/image/ImageService.java)
  would change: `store()` writes to R2 and returns its public URL. Nothing else in the code
  knows where the bytes live.
- To move, copy the rows from `images` into the bucket and update `image_url` in
  `products` and `packaging_options`.

## Safety
- Only an admin can upload (`X-Admin-Key`). Anyone can view a photo.
- The file type is decided from the file's contents, not its name or header. **SVG is
  refused** because it can contain scripts, and so is anything that is not JPEG, PNG or WebP.
- A product's photo address must look like `/api/images/<uuid>`, so it cannot be pointed
  at another website.
- Uploads over 1 MB are refused, and the upload route is the only one allowed a body
  larger than 20 KB.
- The storefront's CSP already allows images from its own origin only.
