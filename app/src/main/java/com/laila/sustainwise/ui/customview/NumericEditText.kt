package com.laila.sustainwise.ui.customview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class NumericEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        // Optionally add custom logic for numeric validation
        super.onTextChanged(text, start, before, count)
    }
}
