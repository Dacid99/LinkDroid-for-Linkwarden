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
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL_DEFAULT = "https://www.linkwarden.com";
    private static final Boolean STORE_CAMERA_PHOTOS_DEFAULT = true;
    private WebView webView;
    private ImageView appImage;
    public SwipeRefreshLayout refresher;
    public static BadgeDrawable badgeDrawable;
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
    public BottomNavigationView navigationView = null;
    public static boolean doNotDoubleLoad = false;
    public static boolean performMenuSelection = true;
    public static boolean subsOverlayVisibility = false;
    private Uri mCapturedImageURI;
    private File photoFile = null;
    public static String currentLang = "en";
    public static boolean switchLang = false;
    private Handler langHandler;
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
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setUserAgentString("com.linkwarden.android");
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
        webView.addJavascriptInterface(javaScriptInterface, "native");

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

        navigationView = findViewById(R.id.bottomNav);
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu1) {
                    if (MainActivity.doNotDoubleLoad) {
                        MainActivity.doNotDoubleLoad = false;
                        return true;
                    }
                    MainActivity.performMenuSelection = false;
                    webView.loadUrl(baseURL + "/");
                    return true;
                } else if (item.getItemId() == R.id.menu2) {
                    if (MainActivity.doNotDoubleLoad) {
                        MainActivity.doNotDoubleLoad = false;
                        return true;
                    }
                    MainActivity.performMenuSelection = false;
                    webView.loadUrl("javascript:(function(){ document.getElementById('inpLocationId').value = 0; window.vue.bShowAddPlant = true; })();");
                    return true;
                } else if (item.getItemId() == R.id.menu3) {
                    if (MainActivity.doNotDoubleLoad) {
                        MainActivity.doNotDoubleLoad = false;
                        return true;
                    }
                    MainActivity.performMenuSelection = false;
                    webView.loadUrl(baseURL + "/links");
                    return true;
                } else if (item.getItemId() == R.id.menu4) {
                    if (MainActivity.doNotDoubleLoad) {
                        MainActivity.doNotDoubleLoad = false;
                        return true;
                    }
                    MainActivity.performMenuSelection = false;
                    webView.loadUrl(baseURL + "/links/pinned");
                    return true;
                } else if (item.getItemId() == R.id.menu5) {
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                return false;
            }
        });

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

        langHandler = new Handler();
        final Runnable langRunnable = new Runnable() {
            @Override
            public void run() {
                if (MainActivity.switchLang) {
                    if (Objects.equals(MainActivity.currentLang, "de")) {
                        navigationView.getMenu().getItem(0).setTitle("Home");
                        navigationView.getMenu().getItem(1).setTitle("Hinzufügen");
                        navigationView.getMenu().getItem(2).setTitle("Aufgaben");
                        navigationView.getMenu().getItem(3).setTitle("Inventar");
                        navigationView.getMenu().getItem(4).setTitle("Suche");
                    } else {
                        navigationView.getMenu().getItem(0).setTitle("Home");
                        navigationView.getMenu().getItem(1).setTitle("Add");
                        navigationView.getMenu().getItem(2).setTitle("Tasks");
                        navigationView.getMenu().getItem(3).setTitle("Inventory");
                        navigationView.getMenu().getItem(4).setTitle("Search");
                    }

                    MainActivity.switchLang = false;
                }

                langHandler.postDelayed(this, 2000);
            }
        };
        langHandler.postDelayed(langRunnable, 1000);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (errorResponse.getStatusCode() == 403) {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                    dlgAlert.setMessage(getStringResource("errorAccessForbiddenBody_" + MainActivity.currentLang));
                    dlgAlert.setTitle(getStringResource("errorAccessForbiddenTitle_" + MainActivity.currentLang));
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

                if (!MainActivity.webAppLoaded) {
                    MainActivity.webAppLoaded = true;
                }

                view.loadUrl("javascript:(function(){ let app = document.getElementById('app'); if (app) { app.style.marginBottom = '50px'; } })();");
                view.loadUrl("javascript:(function(){ let nav = document.getElementsByTagName('nav'); if (nav[0].classList.contains('navbar')) { nav[0].style.backgroundColor = '#454545'; nav[0].classList.add('is-fixed-top'); } })();");
                view.loadUrl("javascript:(function(){ let sorting = document.getElementsByClassName('nav-sorting'); if (sorting !== null) { sorting[0].style.marginTop = '52px'; } })();");
                view.loadUrl("javascript:(function(){ let container = document.getElementsByClassName('container'); if (container) { container[0].style.marginTop = '45px'; } })();");
                view.loadUrl("javascript:(function(){ let modalCards = document.getElementsByClassName('modal-card'); if (modalCards) { for (let i = 0; i < modalCards.length; i++) { modalCards[i].style.maxHeight = '85%'; } } })();");
                view.loadUrl("javascript:(function(){ window.native.setTaskCount(window.currentOpenTaskCount); })();");
                view.loadUrl("javascript:(function(){ let elapp = document.getElementById('app'); if (elapp) { let c = document.createElement('span'); c.id = 'scroller-top'; elapp.insertBefore(c, elapp.firstChild); } })();");
                view.loadUrl("javascript:(function(){ let scroll = document.getElementsByClassName('scroll-to-top'); if (scroll !== null) { scroll[0].style.bottom = '69px'; let inner = document.querySelector('.scroll-to-top-inner'); if (inner) { inner.innerHTML = '<a href=\"javascript:void(0);\" onclick=\"document.querySelector(\\'#scroller-top\\').scrollIntoView({behavior: \\'smooth\\'});\"><i class=\"fas fa-arrow-up fa-2x up-color\"></i></a>'; } } })();");
                view.loadUrl("javascript:(function(){ let radio = document.getElementsByTagName('input'); for (let i = 0; i < radio.length; i++) { if (radio[i].type === 'radio') { radio[i].style.position = 'relative'; radio[i].style.top = '4px'; } } })();");
                view.loadUrl("javascript:(function(){ let file = document.getElementsByTagName('input'); for (let i = 0; i < file.length; i++) { if (file[i].type === 'file') { file[i].accept = 'image/*;capture=camera'; } } })();");
                view.loadUrl("javascript:(function(){ window.native.setCurrentLanguage(window.currentLocale); })();");

                if (MainActivity.performMenuSelection) {
                    if (url.equals(baseURL + "/")) {
                        MainActivity.doNotDoubleLoad = true;
                        setOpenNavMenu(0);
                    } else if (url.equals(baseURL + "/tasks")) {
                        MainActivity.doNotDoubleLoad = true;
                        setOpenNavMenu(2);
                    } else if (url.equals(baseURL + "/inventory")) {
                        MainActivity.doNotDoubleLoad = true;
                        setOpenNavMenu(3);
                    } else if (url.equals(baseURL + "/search")) {
                        MainActivity.doNotDoubleLoad = true;
                        setOpenNavMenu(4);
                    } else {
                        uncheckAllBottomMenuItems();
                    }
                }

                MainActivity.performMenuSelection = true;
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

                    Intent chooserIntent = Intent.createChooser(contentIntent, getStringResource("selectMedia_" + MainActivity.currentLang));
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
                if (!isURLReachable(baseURL + "/")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webAppLoaded = false;

                            try {
                                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                                dlgAlert.setMessage(getStringResource("errorNoConnectionBody_" + MainActivity.currentLang));
                                dlgAlert.setTitle(getStringResource("errorNoConnectionTitle_" + MainActivity.currentLang));
                                dlgAlert.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                dlgAlert.create().show();
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, getStringResource("errorNoConnectionTitle_" + MainActivity.currentLang), Toast.LENGTH_LONG).show();
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
                Toast.makeText(MainActivity.this, getStringResource("errorPermissionRequestDenied_" + MainActivity.currentLang), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setOpenNavMenu(int menu) {
        if (menu == 0) {
            navigationView.setSelectedItemId(R.id.menu1);
        } else if (menu == 1) {
            navigationView.setSelectedItemId(R.id.menu2);
        } else if (menu == 2) {
            navigationView.setSelectedItemId(R.id.menu3);
        } else if (menu == 3) {
            navigationView.setSelectedItemId(R.id.menu4);
        } else if (menu == 4) {
            navigationView.setSelectedItemId(R.id.menu5);
        }
    }

    public void uncheckAllBottomMenuItems()
    {
        navigationView.getMenu().setGroupCheckable(0, true, false);

        for (int i = 0; i < 5; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }

        navigationView.getMenu().setGroupCheckable(0, true, true);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (this.webView.canGoBack()) {
            if (MainActivity.lastLoadedUrl.equals(baseURL + "/")) {
                super.onBackPressed();
                return;
            }

            this.webView.goBack();
        } else {
            super.onBackPressed();
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

class JavaScriptInterface {

    @JavascriptInterface
    public void setCurrentLanguage(String lang)
    {
        MainActivity.currentLang = lang;
        MainActivity.switchLang = true;
    }
}