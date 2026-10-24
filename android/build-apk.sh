#!/usr/bin/env bash
# Builds the Dynamic UPI QR Code Android app (v4.7).
# Needs: JDK 17+, aapt, zipalign, apksigner, dx (dalvik-exchange), android-23.jar
# Usage: KS=path/to/keystore KSPASS=password ./build-apk.sh
set -euo pipefail
cd "$(dirname "$0")"
SITE=".."; OUT="build"; JAR="${ANDROID_JAR:-/usr/share/java/com.android.android-23.jar}"
DX="${DX_JAR:-/usr/share/java/com.android.dx.jar}"
rm -rf "$OUT"; mkdir -p "$OUT/classes" "$OUT/assets/www/fonts"
for f in index.html qrcode.js jsqr.js logos.js banks.js favicon.svg icon-180.png icon-192.png icon-512.png icon-maskable-512.png manifest.webmanifest NOTICE.txt; do cp "$SITE/$f" "$OUT/assets/www/"; done
cp "$SITE"/fonts/*.woff2 "$OUT/assets/www/fonts/"
javac -nowarn -source 8 -target 8 -bootclasspath "$JAR" -d "$OUT/classes" $(find src -name '*.java')
java -cp "$DX" com.android.dx.command.Main --dex --output="$OUT/classes.dex" "$OUT/classes"
aapt package -f -M AndroidManifest.xml -S res -A "$OUT/assets" -I "$JAR" -F "$OUT/app-unsigned.apk" \
  --min-sdk-version 29 --target-sdk-version 34 --version-code 48 --version-name 4.7
( cd "$OUT" && aapt add app-unsigned.apk classes.dex >/dev/null )
zipalign -p -f 4 "$OUT/app-unsigned.apk" "$OUT/app-aligned.apk"
apksigner sign --min-sdk-version 24 --v1-signing-enabled false --v2-signing-enabled true --v3-signing-enabled true --ks "$KS" --ks-pass "pass:$KSPASS" --out "$OUT/Dynamic-UPI-QR-Code-v4.7.apk" "$OUT/app-aligned.apk"
apksigner verify -v "$OUT/Dynamic-UPI-QR-Code-v4.7.apk"
echo "Built: $OUT/Dynamic-UPI-QR-Code-v4.7.apk"
