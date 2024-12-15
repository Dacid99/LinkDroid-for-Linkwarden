package com.sbv.linkdroid;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;
import androidx.appcompat.app.AppCompatDelegate;

public class JavaScriptInterface {
    public static final String THEME_LISTENER_NAME = "Android";
    public static final String THEME_LISTENER_SCRIPT = "function listenToThemeChanges() {\n" +
            "    let currentTheme = localStorage.getItem('theme');\n" +
            "    Android.onThemeChanged(currentTheme);\n" +
            "}\n" +
            "listenToThemeChanges();";
            
    final Context mContext;

    // Constructor
    JavaScriptInterface(Context c) {
        mContext = c;
    }

    // Method to handle theme changes
    @JavascriptInterface
    public void onThemeChanged(final String themeValue) {
        ((Activity) mContext).runOnUiThread(() -> {
            if (themeValue != null) {
                if (themeValue.equals("dark")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
    }
}