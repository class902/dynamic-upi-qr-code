# Dynamic UPI QR Code

Free, private UPI QR code generator. Create a payment QR with a pre-filled amount and note that works with Google Pay, PhonePe, Paytm, BHIM and every other UPI app.

**Live:** https://class902.github.io/dynamic-upi-qr-code/

## Features
- Payee name, UPI ID / mobile number / bank account + IFSC / Aadhaar address types
- Optional amount and note (validated, up to 2 decimals)
- **Upload any existing UPI QR** (image, drag & drop or paste): name, UPI ID, amount and note are read automatically and turned into a fresh dynamic QR
- Three premium card designs: Emerald Aurora, Midnight Gold, Bharat Tricolor
- Quick amount chips, UPI handle suggestions, in-field paste, Clear / Undo, saved QRs (on-device)
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
- `jsqr.js` – Cosmo Wolfe, Apache-2.0 (QR reading).
- BHIM, UPI, Google Pay, PhonePe and Paytm names and logos (`logos.js`) are trademarks of their respective owners, shown only to indicate accepted payment apps. No affiliation or endorsement.

## Disclaimer
Independent tool; not affiliated with NPCI, BHIM, any bank or payment app. Always verify the payee name in your UPI app before paying.

## License
MIT – see `LICENSE`.
