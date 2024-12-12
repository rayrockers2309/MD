package com.laila.sustainwise.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laila.sustainwise.R
import com.laila.sustainwise.ui.signup.HistoryItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val transactions: List<HistoryItem>,
    private val onDeleteTransaction: (String, String, Int) -> Unit // Add the delete callback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_INCOME = 1
        private const val TYPE_EXPENSE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_INCOME -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_income_delete, parent, false)
                IncomeViewHolder(view)
            }
            TYPE_EXPENSE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_expense_delete, parent, false)
                ExpenseViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val transaction = transactions[position]
        when (holder) {
            is IncomeViewHolder -> holder.bind(transaction)
            is ExpenseViewHolder -> holder.bind(transaction)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val transaction = transactions[position]
        return when (transaction.type) {
            "Income" -> TYPE_INCOME
            "Outcome" -> TYPE_EXPENSE
            else -> throw IllegalArgumentException("Invalid transaction type")
        }
    }

    override fun getItemCount(): Int = transactions.size

    inner class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nominalTextView: TextView = itemView.findViewById(R.id.tvNominalIncome)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tvCategoryIncome)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDateIncome)
        private val btnDel: ImageView = itemView.findViewById(R.id.ivDeleteIcon)

        fun bind(transaction: HistoryItem) {
            nominalTextView.text = formatRupiah(transaction.amount)
            categoryTextView.text = transaction.category
            dateTextView.text = formatDate(transaction.date) // Use formatDate here



            btnDel.setOnClickListener {
                // Call the delete callback when the delete button is clicked
                onDeleteTransaction(transaction.type, transaction.id, adapterPosition)
            }
        }
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nominalTextView: TextView = itemView.findViewById(R.id.tvNominalExpense)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tvCategoryExpense)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDateExpense)
        private val btnDel: ImageView = itemView.findViewById(R.id.ivDeleteIcon)

        fun bind(transaction: HistoryItem) {
            nominalTextView.text = formatRupiah(transaction.amount)
            categoryTextView.text = transaction.category
            dateTextView.text = formatDate(transaction.date) // Use formatDate here



            btnDel.setOnClickListener {
                // Call the delete callback when the delete button is clicked
                onDeleteTransaction(transaction.type, transaction.id, adapterPosition)
            }
        }
    }

    private fun formatRupiah(amount: String): String {
        try {
            val amountValue = amount.toDouble()
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID")) // Use the Indonesian locale
            return format.format(amountValue)
        } catch (e: Exception) {
            e.printStackTrace()
            return amount // Return the original string if it can't be parsed
        }
    }

    fun formatDate(dateString: String): String {
        try {
            // Define the input date format (adjust based on the format you receive from the API)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Adjust the format as needed
            val date = inputFormat.parse(dateString)

            // Define the output date format
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return outputFormat.format(date ?: Date()) // Return the current date if parsing fails
        } catch (e: Exception) {
            e.printStackTrace()
            return dateString // Return the original string if there's an error
        }
    }
}

