package com.laila.sustainwise.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laila.sustainwise.R
import com.laila.sustainwise.ui.signup.TransactionItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeAdapter(
    private val transactions: List<TransactionItem>,
    private val onItemClick: (String, String, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_INCOME = 1
        private const val TYPE_EXPENSE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_INCOME -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_income, parent, false)
                IncomeViewHolder(view)
            }
            TYPE_EXPENSE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_expense, parent, false)
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

        fun bind(transaction: TransactionItem) {
            Log.d("HomeAdapter", "Binding Income transaction: $transaction") // Log the transaction data
            nominalTextView.text = formatRupiah(transaction.amount)
            categoryTextView.text = transaction.category
            dateTextView.text = formatDate(transaction.date)

            itemView.setOnClickListener {
                onItemClick(transaction.amount, transaction.category, adapterPosition)
            }
        }
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nominalTextView: TextView = itemView.findViewById(R.id.tvNominalExpense)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tvCategoryExpense)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDateExpense)

        fun bind(transaction: TransactionItem) {
            Log.d("HomeAdapter", "Binding Expense transaction: $transaction") // Log the transaction data
            nominalTextView.text = formatRupiah(transaction.amount)
            categoryTextView.text = transaction.category
            dateTextView.text = formatDate(transaction.date)

            itemView.setOnClickListener {
                onItemClick(transaction.amount, transaction.category, adapterPosition)
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
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Adapt format if necessary
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            e.printStackTrace()
            return dateString // Return original date if parsing fails
        }
    }
}
