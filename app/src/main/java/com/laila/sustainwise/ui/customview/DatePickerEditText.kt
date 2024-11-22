package com.laila.sustainwise.ui.customview

import android.app.DatePickerDialog
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import java.util.Calendar

class DatePickerEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnClickListener {

    init {
        isFocusable = false
        setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                // Validasi: Tanggal tidak boleh di masa depan
                if (selectedDate.after(Calendar.getInstance())) {
                    error = "Tanggal tidak bisa lebih dari hari ini"
                } else {
                    setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun validateDate(): Boolean {
        return if (TextUtils.isEmpty(text)) {
            error = "Tanggal tidak boleh kosong"
            false
        } else {
            error = null // Clear error if valid
            true
        }
    }
}
