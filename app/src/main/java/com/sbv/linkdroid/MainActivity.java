package com.sbv.linkdroid;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL_DEFAULT = "";
    private static final String DASHBOARD_PAGE = "/dashboard";
    private DrawerLayout drawerLayout;
    private WebView webView;
    public SwipeRefreshLayout refresher;
    private SharedPreferences preferences = null;
    private String homeURL, baseURL;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    public static boolean webAppLoaded = false;
    private Handler swipeHandler;
    public static String lastLoadedUrl = "";
    private RelativeLayout imageOverlay;
    private boolean drawerWasOpened = false;

    // Helper method to check if a URL is reachable
    private boolean isURLReachable(String address) {
        try {
            URL url = new URL(address);
            int code;
            if ("https".equals(url.getProtocol())) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                if (preferences.getBoolean("ALLOW_INSECURE_CONNECTION", false)) {
                    connection.setSSLSocketFactory(Utils.createInsecureSslSocketFactory());
                    connection.setHostnameVerifier(Utils.getInsecureHostnameVerifier());
                }
                code = connection.getResponseCode();
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                code = connection.getResponseCode();
            }

            return code == 200 || code == 403;
        } catch (IOException e) {
            Log.d("Error", "In isURLReachable an IOException occurred:" + e);
            return false;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        MaterialButton toBrowserButton = findViewById(R.id.toBrowserButton);
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        ImageButton closeSettingsButton = findViewById(R.id.closeSettingsButton);
        webView = findViewById(R.id.webview);
        refresher = findViewById(R.id.swiperefresh);
        imageOverlay = findViewById(R.id.imageOverlay);

        // Request permissions
        requestNofifications();

        // Initialize preferences and settings
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        preferences = getDefaultSharedPreferences(this);
        baseURL = preferences.getString("BASE_URL", BASE_URL_DEFAULT);
        homeURL = baseURL + DASHBOARD_PAGE;

        // Check if we need to open settings drawer
        if (baseURL.isEmpty()) {
            drawerLayout.post(() -> drawerLayout.openDrawer(GravityCompat.END));
        }

        // Set up a listener for shared preference changes
        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
                if (key != null) {
                    if (key.equals("BASE_URL")) {
                        baseURL = sharedPreferences.getString(key, MainActivity.BASE_URL_DEFAULT);
                        homeURL = baseURL + DASHBOARD_PAGE;
                    }
                } else {
                    baseURL = MainActivity.BASE_URL_DEFAULT;
                    homeURL = baseURL + DASHBOARD_PAGE;
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        // Set up drawer controls
        settingsButton.setOnClickListener(view -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Set up close settings button
        closeSettingsButton.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });

        // Initialize WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(MainActivity.this), JavaScriptInterface.THEME_LISTENER_NAME);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setUserAgentString("com.sbv.linkdroid");
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        // Set up a click listener for the "Open in Browser" button
        toBrowserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView.getUrl() == null){
                    Log.d("Browser", "WebView has no loaded URL.");
                    return;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(webView.getUrl()));
                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(browserIntent);
                } else {
                    Log.d("Browser", "No browser found, adding type text/html ..");
                    Intent fallbackIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setDataAndType(Uri.parse(webView.getUrl()), "text/html");
                    if (fallbackIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "No browser found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        webView.setVisibility(View.GONE);

        // Set up the swipe-to-refresh functionality
        refresher.setOnRefreshListener(() -> {
                    WebBackForwardList forwardList = webView.copyBackForwardList();
                    if (forwardList.getCurrentIndex() == -1) {
                        launchWebsite();
                    } else {
                        webView.reload();
                    }
                    refresher.setRefreshing(false);
                }
        );

        // Delay the swipe-to-refresh functionality to avoid conflicts
        swipeHandler = new Handler(Looper.getMainLooper());
        final Runnable swipeRunnable = new Runnable() {
            @Override
            public void run() {
                refresher.setEnabled(true);
                swipeHandler.postDelayed(this, 500);
            }
        };
        swipeHandler.postDelayed(swipeRunnable, 1000);

        // Set up the WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }
        
            @Override
            public void onPageFinished(WebView view, String url) {
                imageOverlay.setVisibility(View.GONE);
                
                if (webView.getVisibility() == View.GONE) {
                    webView.setVisibility(View.VISIBLE);
                }
        
                MainActivity.lastLoadedUrl = url;
                Log.d("lastLoadedUrl", lastLoadedUrl);
        
                webView.evaluateJavascript(JavaScriptInterface.THEME_LISTENER_SCRIPT, null);
        
                if (!MainActivity.webAppLoaded) {
                    MainActivity.webAppLoaded = true;
                }
        
                CookieManager.getInstance().flush();
            }
        
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d("WebView", "Processing URL: " + url);
        
                if (url.toLowerCase().startsWith(baseURL.toLowerCase())) {
                    return false;
                }
        
                boolean useExternalBrowser = preferences.getBoolean("EXTERNAL_BROWSER", false);
                boolean isSpecialDomain = url.toLowerCase().contains("x.com") || 
                                        url.toLowerCase().contains("twitter.com");
                
                if (useExternalBrowser || isSpecialDomain) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(browserIntent);
                        return true;
                    } catch (Exception e) {
                        try {
                            Intent chooserIntent = Intent.createChooser(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                                "Open with"
                            );
                            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(chooserIntent);
                            return true;
                        } catch (Exception e2) {
                            Log.e("WebView", "Error opening URL: " + e2.getMessage());
                            Toast.makeText(MainActivity.this, 
                                "Could not open link: " + e2.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (preferences.getBoolean("ALLOW_INSECURE_CONNECTION", false)) {
                    handler.proceed();
                    return;
                }

                super.onReceivedSslError(view, handler, error);
            }
        });

        // Set up the back button behavior
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else if (webView.canGoBack()) {
                    webView.goBack();
                } else if (MainActivity.lastLoadedUrl.equals(homeURL)) {
                    finish();
                } else {
                    webView.loadUrl(homeURL);
                }
            }
        });

        // Only launch the website if we have a URL configured
        if (!baseURL.isEmpty()) {
            launchWebsite();
        }
    }

    // Request notification permissions
    private void requestNofifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {
                showNotificationPermissionSnackbar();
            } else {
                Log.d("permissions", "Notification permission already granted");
            }
        }
    }

    private void showNotificationPermissionSnackbar() {
        Snackbar snackbar = Snackbar.make(drawerLayout, getResources().getString(R.string.snackbarText), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getResources().getString(R.string.snackbarButtonText), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{"android.permission.POST_NOTIFICATIONS"}, 100);
            }
        });
        snackbar.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("permissions", "Notification permission successfully granted");
            } else {
                Log.d("permissions", "Notification permission denied");
            }
        }
    }

    private void updateWebTheme() {
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        String theme = (nightMode == AppCompatDelegate.MODE_NIGHT_YES) ? "dark" : "light";
        Log.d("theme", "app theme has changed");
        webView.evaluateJavascript("localStorage.setItem('theme', '" + theme + "'); ", null);
        Log.d("theme", "webtheme changed to " + theme);
    }

    public void launchWebsite() {
        Thread launcher = new Thread() {
            @Override
            public void run() {
                if (!isURLReachable(homeURL)) {
                    runOnUiThread(() -> {
                        webAppLoaded = false;
                        try {
                            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                            dlgAlert.setMessage(getResources().getString(R.string.errorNoConnectionBody));
                            dlgAlert.setTitle(getResources().getString(R.string.errorNoConnectionTitle));
                            dlgAlert.setPositiveButton("Ok", (dialog, which) -> {});
                            dlgAlert.create().show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), 
                                getResources().getString(R.string.errorNoConnectionTitle), 
                                Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> webView.loadUrl(homeURL));
                }
            }
        };
        launcher.start();
    }

    public void reloadWebsite() {
        drawerLayout.closeDrawer(GravityCompat.END);
        if (webView != null) {
            // Clear any overlay
            if (imageOverlay != null) {
                imageOverlay.setVisibility(View.VISIBLE);
            }
            
            // Reset the webview state
            webAppLoaded = false;
            webView.setVisibility(View.GONE);
            
            // Launch the website
            launchWebsite();
        }
    }

    @Override
    public void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        CookieManager.getInstance().flush();
        super.onDestroy();
    }
}