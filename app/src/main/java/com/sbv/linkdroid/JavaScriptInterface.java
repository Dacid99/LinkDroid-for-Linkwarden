package com.sbv.linkdroid;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;

public class JavaScriptInterface {
    public static final String THEME_LISTENER_NAME = "Android";
    public static final String THEME_LISTENER_SCRIPT = "function listenToThemeChanges() {\n" +
            "    let currentTheme = localStorage.getItem('theme');\n" +
            "\n" +
            "    setInterval(function() {\n" +
            "        let newTheme = localStorage.getItem('theme');\n" +
            "        if (newTheme !== currentTheme) {\n" +
            "            currentTheme = newTheme;\n" +
            "            console.log('Theme changed to: ' + newTheme);\n" +
            "            Android.onThemeChanged(newTheme);\n" +
            "        }\n" +
            "    }, 1000);\n" +
            "}\n" +
            "\n" +
            "listenToThemeChanges();";
    Context mContext;

    JavaScriptInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void onThemeChanged(final String themeValue) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (themeValue.equals("dark")) {
                    // Change app theme to dark
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Log.d("theme", "Set to dark mode " + (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES));
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Log.d("theme", "Set to light mode " + (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_NO));
                }
            }
        });
    }
}
