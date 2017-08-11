package com.klinker.android.twitter.views.text

import android.content.Context
import android.support.text.emoji.widget.EmojiTextViewHelper
import android.support.v7.widget.AppCompatTextView
import android.text.InputFilter
import android.util.AttributeSet
import com.klinker.android.twitter.data.EmojiStyle
import com.klinker.android.twitter.settings.AppSettings

open class EmojiableTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : AppCompatTextView(context, attrs, defStyleAttr) {

    private val useEmojiCompat: Boolean
        get() = AppSettings.getInstance(context).emojiStyle != EmojiStyle.DEFAULT

    private var helper: EmojiTextViewHelper? = null
    private val emojiHelper: EmojiTextViewHelper
        get() {
            if (helper == null) {
                helper = EmojiTextViewHelper(this)
            }
            return helper as EmojiTextViewHelper
        }

    init {
        if (useEmojiCompat) {
            emojiHelper.updateTransformationMethod()
        }
    }

    override fun setFilters(filters: Array<InputFilter>) {
        if (useEmojiCompat) {
            super.setFilters(emojiHelper.getFilters(filters))
        } else {
            super.setFilters(filters)
        }
    }

    override fun setAllCaps(allCaps: Boolean) {
        super.setAllCaps(allCaps)
        emojiHelper.setAllCaps(allCaps)
    }

}