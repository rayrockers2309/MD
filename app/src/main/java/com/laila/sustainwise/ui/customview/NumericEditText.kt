package com.laila.sustainwise.ui.customview

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout

class NumericEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    private var parentTextInputLayout: TextInputLayout? = null

    init {
        // Set input type to numbers only
        inputType = InputType.TYPE_CLASS_NUMBER

        // Limit maximum length to 10 characters
        filters = arrayOf(InputFilter.LengthFilter(10))

        // Add text watcher to validate input
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInput(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Find the parent TextInputLayout if it exists
        if (parent is TextInputLayout) {
            parentTextInputLayout = parent as TextInputLayout
        }
    }

    private fun validateInput(input: CharSequence?) {
        if (input == null || input.isEmpty()) {
            parentTextInputLayout?.error = "Input tidak boleh kosong"
            return
        }

        if (input.length > 10) {
            parentTextInputLayout?.error = "Maksimal 10 digit angka"
        } else {
            parentTextInputLayout?.error = null
        }
    }
}
