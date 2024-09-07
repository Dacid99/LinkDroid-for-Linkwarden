package com.linkwarden.android;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL_DEFAULT = "https://www.linkwarden.com";
    private WebView webView;
    private ImageView appImage;
    public SwipeRefreshLayout refresher;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_CAMERA = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private SharedPreferences preferences = null;
    private String baseURL;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    public static boolean refresherVisibility = true;
    public static boolean webAppLoaded = false;
    private Handler swipeHandler;
    public static String lastLoadedUrl = "";
    public static boolean subsOverlayVisibility = false;
    private Uri mCapturedImageURI;
    private File photoFile = null;
    public static String photoFormattedName = "";

    private boolean isURLReachable(String address)
    {
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            int code = connection.getResponseCode();
            return code == 200 || code == 403;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getStringResource(String name)
    {
        @SuppressLint("DiscouragedApi")
        int id = getResources().getIdentifier(name, "string", getPackageName());
        return getResources().getString(id);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getDefaultSharedPreferences(this);

        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
                if (key != null){
                     if (key.equals("BASE_URL")){
                        baseURL = sharedPreferences.getString(key, MainActivity.BASE_URL_DEFAULT);
                    }
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);


        baseURL = preferences.getString("BASE_URL", MainActivity.BASE_URL_DEFAULT);

        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setUserAgentString("com.linkwarden.android");
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        appImage = findViewById(R.id.imgAppIcon);

        webView.setVisibility(View.GONE);

        refresher = findViewById(R.id.swiperefresh);
        refresher.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        WebBackForwardList forwardList = webView.copyBackForwardList();
                        if (forwardList.getCurrentIndex() == -1) {
                            launchWebsite();
                        } else {
                            webView.reload();
                        }
                        refresher.setRefreshing(false);
                    }
                }
        );

        swipeHandler = new Handler();
        final Runnable swipeRunnable = new Runnable() {
            @Override
            public void run() {
                if (MainActivity.refresherVisibility != refresher.isEnabled()) {
                    refresher.setEnabled(MainActivity.refresherVisibility);
                }

                if (MainActivity.subsOverlayVisibility) {
                    refresher.setEnabled(false);
                } else {
                    refresher.setEnabled(MainActivity.refresherVisibility);
                }

                swipeHandler.postDelayed(this, 500);
            }
        };
        swipeHandler.postDelayed(swipeRunnable, 1000);


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (errorResponse.getStatusCode() == 403) {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                    dlgAlert.setMessage(getStringResource("errorAccessForbiddenBody"));
                    dlgAlert.setTitle(getStringResource("errorAccessForbiddenTitle"));
                    dlgAlert.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    dlgAlert.create().show();
                }

                super.onReceivedHttpError(view, request, errorResponse);
            }

            public void onPageFinished(WebView view, String url) {
                if (webView.getVisibility() == View.GONE) {
                    webView.setVisibility(View.VISIBLE);
                    appImage.setVisibility(View.GONE);
                }

                MainActivity.lastLoadedUrl = url;
                Log.d("lastLoadedUrl", lastLoadedUrl);

                if (!MainActivity.webAppLoaded) {
                    MainActivity.webAppLoaded = true;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean shouldOverrideUrlLoading (WebView view,
                                                     WebResourceRequest request)
            {
                return false;
            }


        });

        webView.setWebChromeClient(new WebChromeClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                uploadMessage = filePathCallback;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    File folder = new File(getApplicationContext().getFilesDir(), "captured");
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }

                    String dateTimeExpression = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    photoFormattedName = "IMG_" + dateTimeExpression + ".jpg";
                    photoFile = new File(folder, photoFormattedName);
                    mCapturedImageURI = FileProvider.getUriForFile(getApplicationContext(), "com.linkwarden.android.provider", photoFile);

                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                    Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(contentIntent, getStringResource("selectMedia"));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[] { captureIntent });

                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                }

                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });   // End setWebChromeClient

        launchWebsite();
    }

    public void launchWebsite()
    {
        Thread launcher = new Thread() {
            @Override
            public void run() {
                if (!isURLReachable(baseURL + "/dashboard")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webAppLoaded = false;

                            try {
                                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                                dlgAlert.setMessage(getStringResource("errorNoConnectionBody"));
                                dlgAlert.setTitle(getStringResource("errorNoConnectionTitle"));
                                dlgAlert.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                dlgAlert.create().show();
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, getStringResource("errorNoConnectionTitle"), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(baseURL + "/");
                        }
                    });
                }
            }
        };
        launcher.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_SELECT_CAMERA) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, getStringResource("errorPermissionRequestDenied"), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (this.webView.canGoBack()) {
            if (MainActivity.lastLoadedUrl.equals(baseURL + "/dashboard")) {
                super.onBackPressed();
                return;
            }

            this.webView.goBack();
        } else {
            if (MainActivity.lastLoadedUrl.equals(baseURL + "/dashboard")) {
                super.onBackPressed();
            } else {
                webView.loadUrl(baseURL + "/dashboard");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == uploadMessage)
                return;

            Uri[] results = null;

            uploadMessage.onReceiveValue(results);

            uploadMessage = null;
            mCapturedImageURI = null;
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }
}