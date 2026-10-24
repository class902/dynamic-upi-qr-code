package in.rana.dynamicupiqr;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/** Dynamic UPI QR Code - Android app (v4.7). Hosts the bundled web app in a WebView, fully offline. */
public class MainActivity extends Activity {
    private static final String HOST = "appassets.local";
    private static final String ORIGIN = "https://" + HOST + "/index.html";
    private static final int REQ_FILE = 71;

    private WebView web;
    private ValueCallback<Uri[]> fileCb;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        web.setBackgroundColor(0xFFF4F7FB);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setBuiltInZoomControls(false);
        s.setUseWideViewPort(true);          // honour the page viewport (zoomed-out app layout)
        s.setLoadWithOverviewMode(false);
        s.setUserAgentString(s.getUserAgentString() + " DynamicUPIQR/4.7");

        web.addJavascriptInterface(new Bridge(), "AndroidApp");
        web.setWebViewClient(new AppClient());
        web.setWebChromeClient(new AppChrome());

        if (b != null) {
            web.restoreState(b);
        } else {
            open(getIntent(), true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        setIntent(i);
        open(i, false);
    }

    /** Loads the app, forwarding shared text / pay links that opened it. */
    private void open(Intent i, boolean first) {
        String url = ORIGIN;
        if (i != null) {
            String a = i.getAction();
            if (Intent.ACTION_SEND.equals(a)) {
                String t = i.getStringExtra(Intent.EXTRA_TEXT);
                if (t != null) {
                    try {
                        url = ORIGIN + "?text=" + URLEncoder.encode(t, "UTF-8");
                    } catch (Exception e) {
                        url = ORIGIN;
                    }
                }
            } else if (Intent.ACTION_VIEW.equals(a) && i.getData() != null) {
                String q = i.getData().getEncodedQuery();
                if (q != null) url = ORIGIN + "?" + q;
            }
        }
        if (first || !ORIGIN.equals(url)) web.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.removeJavascriptInterface("AndroidApp");
            web.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == REQ_FILE && fileCb != null) {
            Uri[] r = null;
            if (res == RESULT_OK && data != null && data.getData() != null) r = new Uri[]{data.getData()};
            fileCb.onReceiveValue(r);
            fileCb = null;
        } else {
            super.onActivityResult(req, res, data);
        }
    }

    // ---------- serving the bundled web app ----------
    private static String mime(String p) {
        String l = p.toLowerCase();
        if (l.endsWith(".html")) return "text/html";
        if (l.endsWith(".js")) return "application/javascript";
        if (l.endsWith(".css")) return "text/css";
        if (l.endsWith(".png")) return "image/png";
        if (l.endsWith(".svg")) return "image/svg+xml";
        if (l.endsWith(".woff2")) return "font/woff2";
        if (l.endsWith(".json") || l.endsWith(".webmanifest")) return "application/json";
        if (l.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    private WebResourceResponse serve(Uri u) {
        if (u == null || !HOST.equals(u.getHost())) return null;
        String p = u.getPath();
        if (p == null || p.equals("/")) p = "/index.html";
        p = p.replace("..", "");
        try {
            InputStream in = getAssets().open("www" + p);
            return new WebResourceResponse(mime(p), "UTF-8", in);
        } catch (IOException e) {
            return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found",
                    new HashMap<String, String>(), new ByteArrayInputStream(new byte[0]));
        }
    }

    private class AppClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
            return serve(r.getUrl());
        }

        // UPI app links (upi://, gpay://, phonepe://, paytmmp://) and web links open in the right app.
        @Override
        public boolean shouldOverrideUrlLoading(WebView v, String url) {
            Uri u = Uri.parse(url);
            if (HOST.equals(u.getHost())) return false;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, u));
            } catch (Exception e) {
                String sch = u.getScheme();
                boolean web = "http".equals(sch) || "https".equals(sch);
                Toast.makeText(MainActivity.this, web ? "No browser found" : "No UPI app found to open this payment", Toast.LENGTH_LONG).show();
            }
            return true;
        }
    }

    private class AppChrome extends WebChromeClient {
        @Override
        public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams p) {
            if (fileCb != null) fileCb.onReceiveValue(null);
            fileCb = cb;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            try {
                startActivityForResult(Intent.createChooser(i, "Choose a QR image"), REQ_FILE);
            } catch (Exception e) {
                fileCb = null;
                cb.onReceiveValue(null);
                return false;
            }
            return true;
        }
    }

    // ---------- native features exposed to the web app as window.AndroidApp ----------
    private void toast(final String m) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, m, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Uri store(String name, String mimeType, byte[] data, boolean image) throws IOException {
        ContentResolver r = getContentResolver();
        ContentValues v = new ContentValues();
        v.put("_display_name", name);
        v.put("mime_type", mimeType);
        v.put("relative_path", image ? "Pictures/Dynamic UPI QR" : "Download/Dynamic UPI QR");
        v.put("is_pending", 1);
        Uri col = image ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI : Uri.parse("content://media/external/downloads");
        Uri u = r.insert(col, v);
        if (u == null) throw new IOException("insert failed");
        OutputStream os = r.openOutputStream(u);
        try {
            os.write(data);
        } finally {
            os.close();
        }
        ContentValues d = new ContentValues();
        d.put("is_pending", 0);
        r.update(u, d, null, null);
        return u;
    }

    private static String safeName(String n) {
        String s = n == null ? "upi-qr" : n.replaceAll("[^A-Za-z0-9._-]", "_");
        return s.length() == 0 ? "upi-qr" : s;
    }

    private class Bridge {
        @JavascriptInterface
        public String version() {
            return "4.7";
        }

        @JavascriptInterface
        public void copy(final String t) {
            runOnUiThread(new Runnable() {
                public void run() {
                    ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("UPI", t));
                }
            });
        }

        @JavascriptInterface
        public String readClipboard() {
            FutureTask<String> f = new FutureTask<String>(new Callable<String>() {
                public String call() {
                    ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData d = cm.getPrimaryClip();
                    if (d != null && d.getItemCount() > 0) {
                        CharSequence c = d.getItemAt(0).coerceToText(MainActivity.this);
                        return c == null ? "" : c.toString();
                    }
                    return "";
                }
            });
            runOnUiThread(f);
            try {
                return f.get(1500, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                return "";
            }
        }

        @JavascriptInterface
        public void saveFile(String b64, String name, String mimeType) {
            try {
                byte[] data = Base64.decode(b64, Base64.DEFAULT);
                boolean image = mimeType != null && mimeType.startsWith("image/") && !mimeType.contains("svg");
                store(safeName(name), mimeType == null ? "application/octet-stream" : mimeType, data, image);
                toast(image ? "Saved to Gallery (Pictures/Dynamic UPI QR)" : "Saved to Downloads/Dynamic UPI QR");
            } catch (Exception e) {
                toast("Could not save the file");
            }
        }

        @JavascriptInterface
        public void shareImage(String b64, String name, final String text) {
            try {
                byte[] data = Base64.decode(b64, Base64.DEFAULT);
                final Uri u = store(safeName(name), "image/jpeg", data, true);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent s = new Intent(Intent.ACTION_SEND);
                        s.setType("image/jpeg");
                        s.putExtra(Intent.EXTRA_STREAM, u);
                        s.putExtra(Intent.EXTRA_TEXT, text);
                        s.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(s, "Share QR"));
                    }
                });
            } catch (Exception e) {
                toast("Could not share the image");
            }
        }
    }
}
