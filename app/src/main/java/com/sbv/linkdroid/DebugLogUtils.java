package com.sbv.linkdroid;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowMetrics;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.webkit.WebViewCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DebugLogUtils {
    private static final String TAG = "DebugLogUtils";

    // Method to share the debug log
    public static void shareDebugLog(FragmentActivity activity) {
        try {
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("LinkDroid Debug Log\n\n");
            
            appendSystemInfo(debugInfo, activity);
            appendMemoryInfo(debugInfo, activity);
            appendDisplayInfo(debugInfo, activity);
            appendNetworkInfo(debugInfo, activity);
            appendAppSettings(debugInfo, activity);
            appendWebViewInfo(debugInfo, activity);
            appendRuntimeInfo(debugInfo);

            shareDebugInfo(activity, debugInfo.toString());

        } catch (Exception e) {
            Log.e(TAG, "Error creating debug log", e);
            Toast.makeText(activity, activity.getString(R.string.share_error), Toast.LENGTH_SHORT).show();
        }
    }

    // Append system information to the debug log
    private static void appendSystemInfo(StringBuilder debugInfo, Context context) throws Exception {
        debugInfo.append("=== System Information ===\n");
        debugInfo.append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date())).append("\n");
        debugInfo.append("App Version: ").append(context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0).versionName).append("\n");
        debugInfo.append("Android Version: ").append(Build.VERSION.RELEASE)
                .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        debugInfo.append("Device: ").append(Build.MANUFACTURER)
                .append(" ").append(Build.MODEL).append("\n");
        debugInfo.append("Build: ").append(Build.DISPLAY).append("\n");
        debugInfo.append("Kernel: ").append(System.getProperty("os.version")).append("\n\n");
    }

    // Append memory information to the debug log
    private static void appendMemoryInfo(StringBuilder debugInfo, Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        debugInfo.append("=== Memory Information ===\n");
        debugInfo.append("Total RAM: ").append(memoryInfo.totalMem / 1048576L).append(" MB\n");
        debugInfo.append("Available RAM: ").append(memoryInfo.availMem / 1048576L).append(" MB\n");
        debugInfo.append("Low memory: ").append(memoryInfo.lowMemory).append("\n\n");
    }

    // Append display information to the debug log
    private static void appendDisplayInfo(StringBuilder debugInfo, FragmentActivity activity) {
        debugInfo.append("=== Display Information ===\n");
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = activity.getWindowManager().getCurrentWindowMetrics();
            debugInfo.append("Resolution: ").append(metrics.getBounds().width())
                    .append("x").append(metrics.getBounds().height()).append("\n");
        } else {
            debugInfo.append("Resolution: ").append(displayMetrics.widthPixels)
                    .append("x").append(displayMetrics.heightPixels).append("\n");
        }
        
        debugInfo.append("Density: ").append(displayMetrics.density)
                .append(" (").append(displayMetrics.densityDpi).append(" dpi)\n\n");
    }

    // Append network information to the debug log
    private static void appendNetworkInfo(StringBuilder debugInfo, Context context) {
        debugInfo.append("=== Network Information ===\n");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        
        if (activeNetwork != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
            if (capabilities != null) {
                debugInfo.append("Network Type: ");
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    debugInfo.append("WiFi\n");
                    appendWifiInfo(debugInfo, context);
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    debugInfo.append("Cellular\n");
                } else {
                    debugInfo.append("Other\n");
                }
                debugInfo.append("Metered: ").append(!capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_NOT_METERED)).append("\n");
                debugInfo.append("VPN: ").append(capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_VPN)).append("\n\n");
            }
        } else {
            debugInfo.append("No active network\n\n");
        }
    }

    // Append WiFi information to the debug log
    private static void appendWifiInfo(StringBuilder debugInfo, Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            debugInfo.append("WiFi Enabled: ").append(wifiManager.isWifiEnabled()).append("\n");
        }
    }

    // Append app settings to the debug log
    private static void appendAppSettings(StringBuilder debugInfo, Context context) {
        debugInfo.append("=== App Settings ===\n");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        debugInfo.append("Base URL configured: ").append(!prefs.getString("BASE_URL", "").isEmpty()).append("\n");
        debugInfo.append("Auth Token configured: ").append(!prefs.getString("AUTH_TOKEN", "").isEmpty()).append("\n");
        debugInfo.append("Default Collection: ").append(prefs.getString("COLLECTION_DEFAULT", "")).append("\n");
        debugInfo.append("Name Required: ").append(prefs.getBoolean("NAME_REQUIRED", false)).append("\n\n");
    }

    // Append WebView information to the debug log
    private static void appendWebViewInfo(StringBuilder debugInfo, Context context) {
        debugInfo.append("=== WebView Information ===\n");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PackageInfo webViewPackageInfo = WebViewCompat.getCurrentWebViewPackage(context);
                if (webViewPackageInfo != null) {
                    debugInfo.append("WebView Version: ").append(webViewPackageInfo.versionName).append("\n");
                    debugInfo.append("WebView Package: ").append(webViewPackageInfo.packageName).append("\n\n");
                } else {
                    debugInfo.append("WebView information not available\n\n");
                }
            } else {
                debugInfo.append("WebView information not available on this Android version\n\n");
            }
        } catch (Exception e) {
            debugInfo.append("Error getting WebView information: ").append(e.getMessage()).append("\n\n");
        }
    }

    // Append runtime information to the debug log
    private static void appendRuntimeInfo(StringBuilder debugInfo) {
        debugInfo.append("=== Runtime Information ===\n");
        Runtime runtime = Runtime.getRuntime();
        debugInfo.append("Max Memory: ").append(runtime.maxMemory() / 1048576L).append(" MB\n");
        debugInfo.append("Used Memory: ").append((runtime.totalMemory() - 
                runtime.freeMemory()) / 1048576L).append(" MB\n");
        debugInfo.append("Free Memory: ").append(runtime.freeMemory() / 1048576L).append(" MB\n");
        debugInfo.append("Available Processors: ").append(runtime.availableProcessors()).append("\n");
    }

    // Share the debug information
    private static void shareDebugInfo(FragmentActivity activity, String debugInfo) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.log_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, debugInfo);

        activity.startActivity(Intent.createChooser(shareIntent, 
            activity.getString(R.string.log_chooser_title)));
    }
}