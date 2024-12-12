package com.laila.sustainwise.ui.history

import android.graphics.Typeface
import android.icu.util.Calendar
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.laila.sustainwise.databinding.ItemMonthSliderBinding

class MonthSliderAdapter(
    private val monthsList: List<String>,
    private val onMonthSelected: (String) -> Unit
) : RecyclerView.Adapter<MonthSliderAdapter.MonthViewHolder>() {

    private var selectedPosition = monthsList.indexOf(getCurrentMonth()) // Default to current month

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val binding = ItemMonthSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonthViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val month = monthsList[position]
        holder.bind(month, position == selectedPosition)  // Highlight if selected
    }

    override fun getItemCount(): Int = monthsList.size

    inner class MonthViewHolder(private val binding: ItemMonthSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(month: String, isSelected: Boolean) {
            binding.tvMonth.text = month

            if (isSelected) {
                binding.tvMonth.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)  // Enlarge text
                binding.tvMonth.setTypeface(null, Typeface.BOLD)
                binding.indicatorLine.visibility = View.VISIBLE  // Show underline
            } else {
                binding.tvMonth.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)  // Normal text size
                binding.tvMonth.setTypeface(null, Typeface.NORMAL)
                binding.indicatorLine.visibility = View.GONE  // Hide underline
            }

            // Set a click listener to update selected month
            binding.root.setOnClickListener {
                selectedPosition = adapterPosition
                onMonthSelected(month)
                notifyDataSetChanged()  // Refresh the adapter
            }
        }
    }

    // Get the current month as a string (e.g., "January 2024")
    private fun getCurrentMonth(): String {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return "${getMonthName(currentMonth)} $currentYear"
    }

    // Convert month number to month name
    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "January"
        }
    }
}

