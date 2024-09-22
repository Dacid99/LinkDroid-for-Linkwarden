package com.sbv.linkdroid;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

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
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL_DEFAULT = "";
    private static final String DASHBOARD_PAGE = "/dashboard";
    private DrawerLayout drawerLayout;
    private WebView webView;
    private RelativeLayout imageOverlay;
    public SwipeRefreshLayout refresher;
    private SharedPreferences preferences = null;
    private String homeURL, baseURL;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    public static boolean webAppLoaded = false;
    private Handler swipeHandler;
    public static String lastLoadedUrl = "";

    private boolean isURLReachable(String address)
    {
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            int code = connection.getResponseCode();
            return code == 200 || code == 403;
        } catch (IOException e) {
            Log.d("Error", "In isURLReachable an IOException occured:" + e);
            return false;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        preferences = getDefaultSharedPreferences(this);

        baseURL = preferences.getString("BASE_URL", BASE_URL_DEFAULT);
        homeURL = baseURL + DASHBOARD_PAGE;

        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
                if (key != null){
                    if (key.equals("BASE_URL")){
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

        drawerLayout = findViewById(R.id.drawerLayout);
        View openHandle = findViewById(R.id.openHandle);

        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(MainActivity.this), JavaScriptInterface.THEME_LISTENER_NAME);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setUserAgentString("com.sbv.linkdroid");
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        imageOverlay = findViewById(R.id.imageOverlay);

        webView.setVisibility(View.GONE);

        refresher = findViewById(R.id.swiperefresh);
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

        swipeHandler = new Handler();
        final Runnable swipeRunnable = new Runnable() {
            @Override
            public void run() {
                refresher.setEnabled(true);
                swipeHandler.postDelayed(this, 500);
            }
        };
        swipeHandler.postDelayed(swipeRunnable, 1000);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                openHandle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        openHandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!drawerLayout.isOpen()){
                    openHandle.setVisibility(View.GONE);
                    drawerLayout.openDrawer(GravityCompat.END);
                } else {
                    Log.d("Drawer", "Weird: openHandle was clicked even though drawer is open!");
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (errorResponse.getStatusCode() == 403) {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                    dlgAlert.setMessage(getResources().getString(R.string.errorAccessForbiddenBody));
                    dlgAlert.setTitle(getResources().getString(R.string.errorAccessForbiddenTitle));
                    dlgAlert.setPositiveButton("Ok", (dialog, which) -> {});
                    dlgAlert.create().show();
                }

                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (webView.getVisibility() == View.GONE) {
                    webView.setVisibility(View.VISIBLE);
                    imageOverlay.setVisibility(View.GONE);
                }

                MainActivity.lastLoadedUrl = url;
                Log.d("lastLoadedUrl", lastLoadedUrl);

                webView.evaluateJavascript(JavaScriptInterface.THEME_LISTENER_SCRIPT, null);

                if (!MainActivity.webAppLoaded) {
                    MainActivity.webAppLoaded = true;
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else if (MainActivity.lastLoadedUrl.equals(homeURL)) {
                    finish();
                } else {
                    webView.loadUrl(homeURL);
                }
            }
        });

        requestNofifications();

        launchWebsite();
    }

    private void requestNofifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {
                showNotificationPermissionSnackbar();
            } else {
                Log.d("permissions", "Notification permission already granted");
            }
        }
    }

    private void showNotificationPermissionSnackbar(){
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

    private void updateWebTheme(){
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
                            dlgAlert.setPositiveButton("Ok",
                                    (dialog, which) -> {});
                            dlgAlert.create().show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.errorNoConnectionTitle), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> webView.loadUrl(homeURL));
                }
            }
        };
        launcher.start();
    }

    @Override
    public void onDestroy(){
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }
}