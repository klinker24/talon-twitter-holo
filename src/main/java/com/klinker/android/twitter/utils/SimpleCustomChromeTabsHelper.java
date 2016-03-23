package com.klinker.android.twitter.utils;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.XmlRes;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.util.TypedValue;

import com.klinker.android.twitter.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class SimpleCustomChromeTabsHelper {
    private static final String TAG = "CustomTabsHelper";
    static final String STABLE_PACKAGE = "com.android.chrome";
    static final String BETA_PACKAGE = "com.chrome.beta";
    static final String DEV_PACKAGE = "com.chrome.dev";
    static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final long WARM_UP_ASYNC = 0L;
    private static String sPackageNameToUse;
    private Activity mContext;
    private CustomTabsClient mCustomTabClient;
    private CustomTabsSession mCustomTabSession;
    private CustomTabsCallback mCallback = new CustomTabsCallback() {
        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
            super.onNavigationEvent(navigationEvent, extras);
            for (int i = mCallbacks.size() - 1; i >= 0; i--) {
                mCallbacks.get(i).onNavigationEvent(navigationEvent, extras);
            }

        }

        @Override
        public void extraCallback(String callbackName, Bundle args) {
            super.extraCallback(callbackName, args);
            for (int i = mCallbacks.size() - 1; i >= 0; i--) {
                mCallbacks.get(i).extraCallback(callbackName, args);
            }
        }
    };
    private CustomTabsServiceConnection mCustomTabConnection = new CustomTabsServiceConnection() {
        @Override
        public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
            mCustomTabClient = customTabsClient;
            mCustomTabSession = mCustomTabClient.newSession(mCallback);
            mCustomTabClient.warmup(WARM_UP_ASYNC);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCustomTabClient = null;
        }
    };
    private SparseArrayCompat<CustomTabsCallback> mCallbacks = new SparseArrayCompat<>();
    private CustomTabsIntent.Builder mBuilder;
    private CustomTabFallback mFallback;
    private CustomTabsIntent buildCustomTabIntent(CustomTabsSession customTabSession) {
        mBuilder = new CustomTabsIntent.Builder(customTabSession);
        return mBuilder.setToolbarColor(getThemePrimaryColor(mContext)).setShowTitle(true).build();
    }


    public SimpleCustomChromeTabsHelper(Activity context) {
        mContext = context;
        sPackageNameToUse = getPackageNameToUse(mContext);
    }

    public SimpleCustomChromeTabsHelper(Activity context, @XmlRes int attrs) {
        this(context);
       /* not ready yet */
    }

    public void setFallback(CustomTabFallback fallback){
        mFallback = fallback;
    }
    public void addNavigationCallback(CustomTabsCallback callback) {
        mCallbacks.put(mCallbacks.size(), callback);
    }

    public void openUrl(String url) {
        buildCustomTabIntent(mCustomTabSession).launchUrl(mContext, Uri.parse(url));
    }


    public void openUrl(String url, CustomTabsUiBuilder builder) {
        if (sPackageNameToUse == null && getPackageNameToUse(mContext) == null){
            signalFallback();
            return;
        }
        builder.build(mContext, mCustomTabSession).launchUrl(mContext, Uri.parse(url));
    }

    public void openUrlForResult(String url, int requestCode){
        CustomTabsIntent customTabsIntent = buildCustomTabIntent(mCustomTabSession);
        openUrlForResult(customTabsIntent, url,requestCode);
    }
    public void openUrlForResult(String url, int requestCode, CustomTabsUiBuilder builder){
        CustomTabsIntent customTabsIntent = builder.build(mContext, mCustomTabSession);
        openUrlForResult(customTabsIntent, url,requestCode);
    }
    private void openUrlForResult(CustomTabsIntent customTabsIntent, String url, int requestCode){
        customTabsIntent.intent.setData(Uri.parse(url));
        mContext.startActivityForResult(customTabsIntent.intent, requestCode);
    }
    public void prepareUrl(String url) {
        if (sPackageNameToUse == null && getPackageNameToUse(mContext) == null){
            return;
        }
        if (mCustomTabClient == null || mCustomTabSession == null) {
            CustomTabsClient.bindCustomTabsService(mContext, sPackageNameToUse, mCustomTabConnection);
        } else {
            mCustomTabSession.mayLaunchUrl(Uri.parse(url), null, null);
        }

    }
    private void signalFallback() {
        if(mFallback != null){
            mFallback.onCustomTabsNotAvailableFallback();
        }
    }
    public static int getThemePrimaryColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme ().resolveAttribute (R.attr.colorPrimary, value, true);
        return value.data;
    }


    public static class CustomTabsUiBuilder {

        private HashMap<String, PendingIntent> mMenuItemMap;
        private int mToolbarColor;
        private Bitmap mCloseButton;


        private Bitmap mActionButtonIcon;
        private String mActioButtonDescription;
        private PendingIntent mActionButtonPendingIntent;


        private int mStartEnterAnimationResId;
        private int mStartExitAnimationResid;

        private int mExitEnterAnimationResId;
        private int mExitExitAnimationResid;


        private boolean mShowTitle = false;


        private boolean hasStartAnimationOptions = false;
        private boolean hasExitAnimationOptions = false;
        private Bitmap mExitIcon;


        public CustomTabsUiBuilder setToolbarColor(@ColorInt int color) {
            mToolbarColor = color;
            return this;
        }

        public CustomTabsUiBuilder setCloseButtonIcon(@NonNull Bitmap icon) {
            mCloseButton = icon;
            return this;
        }

        public CustomTabsUiBuilder setShowTitle(boolean showTitle) {
            mShowTitle = showTitle;
            return this;
        }

        public CustomTabsUiBuilder addMenuItem(@NonNull String label, @NonNull PendingIntent pendingIntent) {
            if (mMenuItemMap == null) {
                mMenuItemMap = new HashMap<>();
            }
            mMenuItemMap.put(label, pendingIntent);
            return this;
        }

        public CustomTabsUiBuilder setActionButton(@NonNull Bitmap icon, @NonNull String description, @NonNull PendingIntent pendingIntent) {
            mActionButtonIcon = icon;
            mActioButtonDescription = description;
            mActionButtonPendingIntent = pendingIntent;
            return this;
        }

        public CustomTabsUiBuilder setStartAnimations(@AnimRes int enterResId, @AnimRes int exitResId) {
            mStartEnterAnimationResId = enterResId;
            mStartExitAnimationResid = exitResId;
            hasStartAnimationOptions = true;
            return this;
        }

        public CustomTabsUiBuilder setExitAnimations(@AnimRes int enterResId, @AnimRes int exitResId) {
            mExitEnterAnimationResId = enterResId;
            mExitExitAnimationResid = exitResId;
            hasExitAnimationOptions = true;
            return this;
        }


        private CustomTabsIntent build(Context context, CustomTabsSession customTabSession) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(customTabSession);
            builder.setShowTitle(mShowTitle);
            if (mToolbarColor > -1) {
                builder.setToolbarColor(mToolbarColor);
            }
            if (hasExitAnimationOptions) {
                builder.setExitAnimations(context, mExitEnterAnimationResId, mExitExitAnimationResid);
            }
            if (hasStartAnimationOptions) {
                builder.setStartAnimations(context, mStartEnterAnimationResId, mStartExitAnimationResid);
            }
            if (mActionButtonIcon != null) {
                builder.setActionButton(mActionButtonIcon, mActioButtonDescription, mActionButtonPendingIntent);
            }
            if (mCloseButton != null) {
                builder.setCloseButtonIcon(mCloseButton);
            }
            if (mMenuItemMap != null && !mMenuItemMap.isEmpty()) {
                Iterator<String> iterator = mMenuItemMap.keySet().iterator();
                for (; iterator.hasNext(); ) {
                    String menuItemDescription = iterator.next();
                    PendingIntent menuPendingiIntent = mMenuItemMap.get(menuItemDescription);
                    builder.addMenuItem(menuItemDescription, menuPendingiIntent);
                }
            }
            return builder.build();
        }

    }


    public static String getPackageNameToUse(Context context) {
        if (sPackageNameToUse != null) return sPackageNameToUse;

        PackageManager pm = context.getPackageManager();
        // Get default VIEW intent handler.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
        }

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.isEmpty()) {
            sPackageNameToUse = null;
        } else if (packagesSupportingCustomTabs.size() == 1) {
            sPackageNameToUse = packagesSupportingCustomTabs.get(0);
        } else if (!android.text.TextUtils.isEmpty(defaultViewHandlerPackageName)
                && !hasSpecializedHandlerIntents(context, activityIntent)
                && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
            sPackageNameToUse = defaultViewHandlerPackageName;
        } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            sPackageNameToUse = STABLE_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            sPackageNameToUse = BETA_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            sPackageNameToUse = DEV_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            sPackageNameToUse = LOCAL_PACKAGE;
        }
        return sPackageNameToUse;
    }

    private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER);
            if (handlers == null || handlers.size() == 0) {
                return false;
            }
            for (ResolveInfo resolveInfo : handlers) {
                IntentFilter filter = resolveInfo.filter;
                if (filter == null) continue;
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue;
                if (resolveInfo.activityInfo == null) continue;
                return true;
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Runtime exception while getting specialized handlers");
        }
        return false;
    }
    public interface CustomTabFallback {
        void onCustomTabsNotAvailableFallback();
    }
}