package com.laila.sustainwise.ui.home

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.laila.sustainwise.R
import com.laila.sustainwise.data.retrofit.RetrofitInstance
import com.laila.sustainwise.data.retrofit.TransactionResponse
import com.laila.sustainwise.databinding.FragmentHomeBinding
import com.laila.sustainwise.ui.signup.SaldoResponse
import com.laila.sustainwise.ui.signup.TransactionItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeAdapter: HomeAdapter
    private lateinit var transactionList: MutableList<TransactionItem>
    private lateinit var totalSaldoTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Inisialisasi list transaksi
        transactionList = mutableListOf()

        // Setup adapter untuk RecyclerView
        homeAdapter = HomeAdapter(transactionList) { amount, category, position ->
            // Format amount menjadi Rupiah
            val formattedAmount = formatRupiah(amount.toDoubleOrNull() ?: 0.0)
            // Tampilkan log popup dengan format Rupiah
            Toast.makeText(context, "$formattedAmount from $category", Toast.LENGTH_SHORT).show()
        }


        // Menghubungkan adapter dengan RecyclerView
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)
        binding.rvTransactions.adapter = homeAdapter

        // Setup the total balance TextView
        totalSaldoTextView = binding.tvTotalBalance

        // Fetch saldo
        fetchSaldo()

        // Fetch transaksi terbaru dari API
        fetchUserIdToken { idToken ->
            fetchLatestTransactions(idToken) // Ambil transaksi terbaru
        }

        val toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar?.title = "SustainWise"
        setHasOptionsMenu(true)
        toolbar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.statusBarColor)))

        return binding.root
    }


    // Menampilkan ProgressBar
    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    // Menyembunyikan ProgressBar
    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    // Ambil ID token dari FirebaseAuth
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

    // Fetch saldo
    private fun fetchSaldo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val tokenTask = currentUser.getIdToken(true)
            tokenTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result?.token
                    if (idToken != null) {
                        showProgressBar() // Menampilkan ProgressBar
                        callSaldoApi("Bearer $idToken")
                    }
                } else {
                    Toast.makeText(context, "Your balance was empty", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Call saldo API
    private fun callSaldoApi(authToken: String) {
        val apiService = RetrofitInstance.api
        val call = apiService.getSaldo(authToken)

        call.enqueue(object : Callback<SaldoResponse> {
            override fun onResponse(call: Call<SaldoResponse>, response: Response<SaldoResponse>) {
                hideProgressBar() // Menyembunyikan ProgressBar setelah selesai
                if (response.isSuccessful) {
                    val saldoResponse = response.body()
                    saldoResponse?.let {
                        // Try to convert saldo to Double, if it's a valid numeric value
                        val saldoValue = it.saldo.toString().toDoubleOrNull()

                        if (saldoValue != null) {
                            val formattedSaldo = formatRupiah(saldoValue)
                            totalSaldoTextView.text = formattedSaldo
                        } else {
                            Toast.makeText(context, "Invalid saldo value", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Your balance was empty: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SaldoResponse>, t: Throwable) {
                hideProgressBar() // Menyembunyikan ProgressBar setelah gagal
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fetch data transaksi terbaru dari API
    private fun fetchLatestTransactions(idToken: String) {
        showProgressBar() // Menampilkan ProgressBar saat memulai fetch
        RetrofitInstance.api.getLatestTransactions("Bearer $idToken")
            .enqueue(object : Callback<TransactionResponse> {
                override fun onResponse(call: Call<TransactionResponse>, response: Response<TransactionResponse>) {
                    hideProgressBar() // Menyembunyikan ProgressBar setelah selesai
                    if (response.isSuccessful) {
                        val transactions = response.body()?.transactions ?: emptyList()
                        transactionList.clear()
                        transactionList.addAll(transactions)
                        homeAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "Your Balance is Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                    hideProgressBar() // Menyembunyikan ProgressBar setelah gagal
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID")) // Use the Indonesian locale for IDR
        return format.format(amount)
    }
}


