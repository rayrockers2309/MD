package com.laila.sustainwise.ui.transaction

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.laila.sustainwise.R
import com.laila.sustainwise.data.model.TransactionRequest
import com.laila.sustainwise.data.retrofit.RetrofitInstance
import com.laila.sustainwise.data.retrofit.TransaksiResponse
import com.laila.sustainwise.ui.customview.DatePickerEditText
import com.laila.sustainwise.ui.customview.NumericEditText
import com.laila.sustainwise.ui.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class TransactionActivity : AppCompatActivity() {

    private lateinit var transactionTypeSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var categoryInput: TextInputEditText
    private lateinit var amountInput: NumericEditText
    private lateinit var dateInput: DatePickerEditText
    private lateinit var submitButton: MaterialButton
    private lateinit var categoryLayout: LinearLayout
    private lateinit var spinnerContainer: LinearLayout
    private lateinit var inputContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        // Bind UI components
        transactionTypeSpinner = findViewById(R.id.list_transaction)
        categorySpinner = findViewById(R.id.list_category)
        categoryInput = findViewById(R.id.category_input)
        amountInput = findViewById(R.id.amount_input)
        dateInput = findViewById(R.id.date_input)
        submitButton = findViewById(R.id.submit_button)
        categoryLayout = findViewById(R.id.category_layout)
        spinnerContainer = findViewById(R.id.spinner_container)
        inputContainer = findViewById(R.id.input_container)

        // Spinner options
        val transactionTypes = arrayOf("Income", "Outcome")
        val categories = arrayOf("Food", "Transport", "Lifestyle", "Shopping", "Billing", "Other")

        transactionTypeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            transactionTypes
        )

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        // Listen for transaction type changes
        transactionTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedType = transactionTypes[position]
                updateCategoryInputType(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Date picker setup
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    dateInput.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Submit button logic
        submitButton.setOnClickListener {
            val transactionType = transactionTypeSpinner.selectedItem.toString()
            val category = if (transactionType == "Income") {
                categoryInput.text.toString()
            } else {
                categorySpinner.selectedItem.toString()
            }
            val amount = amountInput.text.toString()
            val date = dateInput.text.toString()

            if (amount.isEmpty() || date.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            firebaseUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result?.token
                    idToken?.let {
                        val transactionRequest = TransactionRequest(
                            type = transactionType,
                            amount = amount,
                            date = date,
                            category = category
                        )
                        submitTransactionUsingRetrofit(it, transactionRequest)
                    }
                } else {
                    Toast.makeText(this, "Failed to get ID token", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateCategoryInputType(type: String) {
        if (type == "Income") {
            spinnerContainer.visibility = View.GONE
            inputContainer.visibility = View.VISIBLE
        } else {
            spinnerContainer.visibility = View.VISIBLE
            inputContainer.visibility = View.GONE
        }
    }

    private fun submitTransactionUsingRetrofit(idToken: String, transactionRequest: TransactionRequest) {
        RetrofitInstance.api.submitTransaction(transactionRequest, "Bearer $idToken")
            .enqueue(object : Callback<TransaksiResponse> {
                override fun onResponse(call: Call<TransaksiResponse>, response: Response<TransaksiResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TransactionActivity, "Transaction saved", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@TransactionActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@TransactionActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TransaksiResponse>, t: Throwable) {
                    Toast.makeText(this@TransactionActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

