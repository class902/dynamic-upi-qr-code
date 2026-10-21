# Dynamic UPI QR Code

Free, private UPI QR code generator. Create a payment QR with a pre-filled amount and note that works with Google Pay, PhonePe, Paytm, BHIM and every other UPI app.

**Live:** https://class902.github.io/dynamic-upi-qr-code/

## Features
- Payee name, UPI ID / mobile number / bank account + IFSC / Aadhaar address types
- Optional amount and note (validated, up to 2 decimals)
- Download print-ready PNG card or vector SVG, copy link, share, print, open in UPI app
- Light/dark theme, installable offline PWA, fully responsive
- 100% client-side: nothing is uploaded, no cookies, no analytics, no ads

## Deploy (GitHub Pages)
Repo **Settings → Pages → Build and deployment → Deploy from a branch → `main` / `/ (root)` → Save.**

## How it works
Builds a standard `upi://pay?pa=…&pn=…&am=…&cu=INR&tn=…` link and encodes it with [qrcode-generator](https://github.com/kazuhikoarase/qrcode-generator) (MIT, bundled as `qrcode.js`).

## Disclaimer
Independent tool; not affiliated with NPCI, BHIM, any bank or payment app. Always verify the payee name in your UPI app before paying.

## License
MIT – see `LICENSE`.
