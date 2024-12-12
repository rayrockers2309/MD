package com.laila.sustainwise.ui.customview

import android.app.DatePickerDialog
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import java.util.Calendar

class DatePickerEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay))
            }, year, month, day).show()
        }
    }
}
