
package com.class902.dynamicupiqr;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://appassets.androidplatform.net/assets/www/index.html?source=android";
    private static final int PICK_IMAGE_REQUEST = 4701;
    private WebView webView;
    private ValueCallback<Uri[]> pendingFileChooser;
    private WebViewAssetLoader assetLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(15, 118, 110));
        getWindow().setNavigationBarColor(Color.rgb(15, 118, 110));
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(244, 247, 251));
        setContentView(webView);
        assetLoader = new WebViewAssetLoader.Builder()
                .setDomain("appassets.androidplatform.net")
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) settings.setSafeBrowsingEnabled(true);
        webView.addJavascriptInterface(new AndroidDownloads(), "AndroidDownloads");
        webView.setWebViewClient(new WebViewClient() {
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return assetLoader.shouldInterceptRequest(Uri.parse(url));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return routeUrl(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return routeUrl(Uri.parse(url));
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (pendingFileChooser != null) pendingFileChooser.onReceiveValue(null);
                pendingFileChooser = callback;
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("image/*");
                try {
                    startActivityForResult(Intent.createChooser(pick, "Choose a QR image"), PICK_IMAGE_REQUEST);
                } catch (Exception ex) {
                    pendingFileChooser = null;
                    callback.onReceiveValue(null);
                    Toast.makeText(MainActivity.this, "Image picker unavailable", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        webView.loadUrl(APP_URL);
    }

    private boolean routeUrl(Uri uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if ((scheme.equals("https") || scheme.equals("http"))
                && "appassets.androidplatform.net".equalsIgnoreCase(uri.getHost())) return false;
        if (scheme.equals("blob") || scheme.equals("data") || scheme.equals("javascript") || scheme.equals("about")) return false;
        try {
            Intent intent = "intent".equals(scheme)
                    ? Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                    : new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (Exception ex) {
            Toast.makeText(this, "No compatible app found for this link", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && pendingFileChooser != null) {
            Uri[] results = resultCode == RESULT_OK && data != null && data.getData() != null
                    ? new Uri[]{data.getData()} : null;
            pendingFileChooser.onReceiveValue(results);
            pendingFileChooser = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    public class AndroidDownloads {
        @JavascriptInterface public void saveBlob(String encoded, String mimeType, String fileName) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
                    String name = safeFileName(fileName);
                    String mime = (mimeType == null || mimeType.isEmpty()) ? "application/octet-stream" : mimeType;
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, name);
                    values.put(MediaStore.Downloads.MIME_TYPE, mime);
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Dynamic UPI QR");
                    values.put(MediaStore.Downloads.IS_PENDING, 1);
                    Uri output = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (output == null) throw new IllegalStateException("Could not create download");
                    try (OutputStream stream = getContentResolver().openOutputStream(output)) {
                        if (stream == null) throw new IllegalStateException("Could not open download");
                        stream.write(bytes);
                    }
                    ContentValues done = new ContentValues();
                    done.put(MediaStore.Downloads.IS_PENDING, 0);
                    getContentResolver().update(output, done, null, null);
                    Toast.makeText(MainActivity.this, "Saved to Downloads / Dynamic UPI QR", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    Toast.makeText(MainActivity.this, "Download failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface public void shareImage(String encoded, String mimeType, String fileName, String title, String text) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
                    File folder = new File(getCacheDir(), "shared");
                    if (!folder.exists() && !folder.mkdirs()) throw new IllegalStateException("Could not prepare share");
                    File image = new File(folder, safeFileName(fileName));
                    try (FileOutputStream stream = new FileOutputStream(image)) { stream.write(bytes); }
                    Uri contentUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", image);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType(mimeType == null || mimeType.isEmpty() ? "image/jpeg" : mimeType);
                    share.putExtra(Intent.EXTRA_STREAM, contentUri);
                    share.putExtra(Intent.EXTRA_TEXT, text);
                    share.putExtra(Intent.EXTRA_SUBJECT, title);
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(share, "Share payment QR"));
                } catch (Exception ex) {
                    Toast.makeText(MainActivity.this, "Could not share the QR image", Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface public void printPage() {
            runOnUiThread(() -> {
                try {
                    PrintManager manager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                    if (manager == null) throw new IllegalStateException("Printing unavailable");
                    PrintAttributes attributes = new PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(new PrintAttributes.Resolution("upi_qr_300dpi", "UPI QR", 300, 300))
                            .setColorMode(PrintAttributes.COLOR_MODE_COLOR).build();
                    manager.print("Dynamic UPI QR 4.7", webView.createPrintDocumentAdapter("Dynamic UPI QR 4.7"), attributes);
                } catch (Exception ex) {
                    Toast.makeText(MainActivity.this, "Printing is unavailable on this device", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private static String safeFileName(String value) {
        String clean = value == null ? "UPI-QR" : value.replaceAll("[^A-Za-z0-9._-]", "_");
        if (clean.isEmpty() || clean.equals(".") || clean.equals("..")) clean = "UPI-QR";
        return clean.length() > 90 ? clean.substring(0, 90) : clean;
    }

    @Override protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
        }
        super.onDestroy();
    }
}
