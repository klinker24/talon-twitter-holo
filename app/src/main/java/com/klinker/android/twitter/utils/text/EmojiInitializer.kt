package com.klinker.android.twitter.utils.text

import android.content.Context
import android.os.Build
import android.support.text.emoji.EmojiCompat
import android.support.text.emoji.FontRequestEmojiCompatConfig
import android.support.v4.provider.FontRequest
import android.util.Log
import com.klinker.android.twitter.R
import com.klinker.android.twitter.data.EmojiStyle
import com.klinker.android.twitter.settings.AppSettings

object EmojiInitializer {

    fun initializeEmojiCompat(context: Context) {
        val fontRequest = when (AppSettings.getInstance(context).emojiStyle) {
            EmojiStyle.ANDROID_O -> createAndroidODownloadRequest()
            else -> null
        }

        if (fontRequest != null) initializeWithRequest(context, fontRequest)
    }

    private fun createAndroidODownloadRequest(): FontRequest {
        return FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)

    }

    private fun initializeWithRequest(context: Context, fontRequest: FontRequest) {
        EmojiCompat.init(FontRequestEmojiCompatConfig(context, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        Log.i("EmojiCompat", "EmojiCompat initialized")
                    }

                    override fun onFailed(throwable: Throwable?) {
                        Log.e("EmojiCompat", "EmojiCompat initialization failed", throwable)
                    }
                }))
    }

    fun isAlreadyUsingGoogleAndroidO(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.MANUFACTURER.toLowerCase() == "google"
    }
}