package com.laila.sustainwise.ui.history

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.laila.sustainwise.data.retrofit.ApiResponse
import com.laila.sustainwise.data.retrofit.RetrofitInstance
import com.laila.sustainwise.databinding.FragmentHistoryBinding
import com.laila.sustainwise.ui.signup.DeleteTransactionResponse
import com.laila.sustainwise.ui.signup.HistoryItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var transactionList: MutableList<HistoryItem>
    private lateinit var monthSliderAdapter: MonthSliderAdapter
    private val monthsList: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        // Initialize the transaction list and adapter
        transactionList = mutableListOf()
        historyAdapter = HistoryAdapter(transactionList) { type, transactionId, position ->
            // Handle delete click
            deleteTransaction(type, transactionId, position)
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)
        binding.rvTransactions.adapter = historyAdapter

        // Setup the month slider
        setupMonthSlider()

        // Fetch user ID token and load transactions for the current month
        fetchUserIdToken { idToken ->
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            fetchTransactions(idToken, "All", currentMonth, currentYear)
        }

        return binding.root
    }

    // Initialize the month slider with months from January 2024 to the current month
    private fun setupMonthSlider() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val monthsList = mutableListOf<String>()

        // Generate months from January 2024 to the current month
        for (year in 2024..currentYear) {
            val endMonth = if (year == currentYear) currentMonth else 12
            for (m in 1..endMonth) {
                val monthName = "${getMonthName(m)} $year"
                monthsList.add(monthName)
            }
        }

        monthSliderAdapter = MonthSliderAdapter(monthsList) { selectedMonth ->
            val (month, year) = selectedMonth.split(" ")
            val monthInt = getMonthInt(month)
            fetchUserIdToken { idToken ->
                fetchTransactions(idToken, "All", monthInt, year.toInt())
            }
        }

        binding.monthSlider.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.monthSlider.adapter = monthSliderAdapter

        // Scroll to the current month
        binding.monthSlider.post {
            val currentMonthPosition = monthsList.indexOf(getCurrentMonth())
            if (currentMonthPosition != -1) {
                (binding.monthSlider.layoutManager as LinearLayoutManager).scrollToPosition(currentMonthPosition)
            }
        }
    }

    // Function to convert month name to month number
    private fun getMonthInt(monthName: String): Int {
        return when (monthName) {
            "January" -> 1
            "February" -> 2
            "March" -> 3
            "April" -> 4
            "May" -> 5
            "June" -> 6
            "July" -> 7
            "August" -> 8
            "September" -> 9
            "October" -> 10
            "November" -> 11
            "December" -> 12
            else -> 1
        }
    }

    // Function to get month name from month number
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

    private fun deleteTransaction(type: String, transactionId: String, position: Int) {
        // Tampilkan dialog konfirmasi sebelum menghapus transaksi
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Jika pengguna memilih "Yes", lanjutkan dengan penghapusan
                dialog.dismiss()
                fetchUserIdToken { idToken ->
                    binding.progressBar.visibility = View.VISIBLE // Show loading spinner
                    RetrofitInstance.api.deleteTransaction("Bearer $idToken", type, transactionId)
                        .enqueue(object : Callback<DeleteTransactionResponse> {
                            override fun onResponse(
                                call: Call<DeleteTransactionResponse>,
                                response: Response<DeleteTransactionResponse>
                            ) {
                                binding.progressBar.visibility = View.GONE // Hide loading spinner
                                if (response.isSuccessful) {
                                    // Remove the item from the list and notify the adapter
                                    transactionList.removeAt(position)
                                    historyAdapter.notifyItemRemoved(position)

                                    val updatedSaldo = response.body()?.updatedSaldo
                                    // Handle updated saldo if needed
                                } else {
                                    Toast.makeText(context, "Failed to delete transaction", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<DeleteTransactionResponse>, t: Throwable) {
                                binding.progressBar.visibility = View.GONE // Hide loading spinner
                                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                // Jika pengguna memilih "No", batalkan penghapusan
                dialog.dismiss()
            }
            .create()
            .show()
    }


    // Fetch transactions from API
    private fun fetchTransactions(idToken: String, type: String, month: Int, year: Int) {
        binding.progressBar.visibility = View.VISIBLE // Tampilkan indikator loading
        RetrofitInstance.api.getTransactions("Bearer $idToken", type, month, year)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    binding.progressBar.visibility = View.GONE // Sembunyikan indikator loading
                    if (response.isSuccessful) {
                        val transactions = response.body()?.transactions ?: emptyList()
                        transactionList.clear()
                        transactionList.addAll(transactions)
                        historyAdapter.notifyDataSetChanged()

                        // Cek apakah daftar transaksi kosong
                        if (transactions.isEmpty()) {
                            binding.tvEmptyMessage.visibility = View.VISIBLE
                            binding.rvTransactions.visibility = View.GONE
                        } else {
                            binding.tvEmptyMessage.visibility = View.GONE
                            binding.rvTransactions.visibility = View.VISIBLE
                        }
                    } else {
                        Log.e(
                            "API_ERROR",
                            "Code: ${response.code()}, Message: ${response.message()}, Params: Type=$type, Month=$month, Year=$year"
                        )
                        when (response.code()) {
                            404 -> {
                                // Handle kasus 404
                                binding.tvEmptyMessage.visibility = View.VISIBLE
                                binding.rvTransactions.visibility = View.GONE
                            }
                            else -> {
                                Toast.makeText(
                                    context,
                                    "Failed to fetch transactions: ${response.message()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE // Sembunyikan indikator loading
                    Log.e("API_FAILURE", "Error: ${t.localizedMessage}, Params: Type=$type, Month=$month, Year=$year")
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }



    // Fetch user ID token from FirebaseAuth
    private fun fetchUserIdToken(onTokenFetched: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(true)
            ?.addOnSuccessListener { result ->
                val idToken = result.token
                if (!idToken.isNullOrEmpty()) {
                    onTokenFetched(idToken)
                } else {
                    Toast.makeText(context, "Failed to fetch ID token.", Toast.LENGTH_SHORT).show()
                }
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentMonth(): String {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return "${getMonthName(currentMonth)} $currentYear"
    }
}


