# Dynamic UPI QR Code

Free, private UPI QR code generator. Create a payment QR with a pre-filled amount and note that works with Google Pay, PhonePe, Paytm, BHIM and every other UPI app.

**Live:** https://class902.github.io/dynamic-upi-qr-code/

## Features
- Payee name, UPI ID / mobile number / bank account + IFSC / Aadhaar address types
- Optional amount and note (validated, up to 2 decimals)
- **Upload any existing UPI QR** (image, drag & drop or paste): name, UPI ID, amount and note are read automatically and turned into a fresh dynamic QR
- **Credit Card QR mode**: pick Axis, ICICI, SBI Card, IDFC FIRST, AU or American Express from a swipeable logo rail; the QR uses each bank's card-bill UPI address. Card numbers are never stored
- Separate colour themes: teal/indigo for UPI QR, violet/pink for Credit Card QR
- **Pay links**: the shared link opens a payer page that launches the UPI app directly (Google Pay, PhonePe, Paytm, BHIM buttons; QR on desktop)
- Optional logo in the QR centre, hide-UPI-ID switch, UPI handle recognition, tap-the-header refresh
- Three premium card designs (swipe left/right on the card, or use arrow keys): Emerald Aurora, Midnight Gold, Bharat Tricolor
- Quick amount chips (₹100 / ₹500 / ₹1,000 / ₹2,000), UPI handle suggestions, in-field paste, Clear / Undo, saved QRs (on-device)
- Premium synthesised success chime when a QR is read (mute with the 🔔 button)
- Installable app (PWA / WebAPK), works offline, accepts shared UPI links
- Download print-ready JPG card or vector SVG, copy link, share, print, open in UPI app
- Light/dark theme, installable offline PWA, fully responsive
- 100% client-side: nothing is uploaded, no cookies, no analytics, no ads

## Deploy (GitHub Pages)
Repo **Settings → Pages → Build and deployment → Deploy from a branch → `main` / `/ (root)` → Save.**

## How it works
Builds a standard `upi://pay?pa=…&pn=…&am=…&cu=INR&tn=…` link and encodes it with [qrcode-generator](https://github.com/kazuhikoarase/qrcode-generator) (MIT, bundled as `qrcode.js`).

## Third-party notices
- `qrcode.js` – Kazuhiko Arase, MIT.
- `banks.js` – bank logos from praveenpuglia/indian-banks and nkscoder/indian-banks; names and logos belong to the respective banks.
- `jsqr.js` – Cosmo Wolfe, Apache-2.0 (QR reading).
- BHIM, UPI, Google Pay, PhonePe and Paytm names and logos (`logos.js`) are trademarks of their respective owners, shown only to indicate accepted payment apps. No affiliation or endorsement.

## Disclaimer
Independent tool; not affiliated with NPCI, BHIM, any bank or payment app. Always verify the payee name in your UPI app before paying.

## License
MIT – see `LICENSE`.
