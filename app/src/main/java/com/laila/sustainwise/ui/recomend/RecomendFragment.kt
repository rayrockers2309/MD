package com.laila.sustainwise.ui.recomend

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.laila.sustainwise.R
import com.laila.sustainwise.data.model.ModelManager
import com.laila.sustainwise.data.retrofit.RetrofitInstance
import com.laila.sustainwise.databinding.FragmentRecomendBinding
import com.laila.sustainwise.ui.signup.StatisticsResponse
import com.laila.sustainwise.ui.signup.WeeklyExpense
import com.laila.sustainwise.ui.signup.WeeklyExpensesResponse
import org.tensorflow.lite.Interpreter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecomendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecomendFragment : Fragment() {

    private lateinit var modelManager: ModelManager
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var recommendationText: TextView
    private lateinit var tvEmptyMessage: TextView
    private lateinit var binding: FragmentRecomendBinding
    private lateinit var monthSliderAdapter: MonthAdapter
    private val monthsList: MutableList<String> = mutableListOf()
    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecomendBinding.inflate(inflater, container, false)

        // Initialize charts and other UI elements
        barChart = binding.barChart
        pieChart = binding.pieChart
        recommendationText = binding.textRecomend
        tvEmptyMessage = binding.tvEmptyMessage
        modelManager = ModelManager(requireContext())  // Use requireContext() here


        if (!modelManager.isModelAvailable()) {
            modelManager.downloadModel()
        } else {
            Log.d("ModelManager", "Model ready: ${modelManager.getModelFile().absolutePath}")
        }

        // Setup month slider
        setupMonthSlider()

        // Fetch user token and data
        fetchUserTokenAndFetchStatistics()

        val toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar?.title = "Recommendation"
        setHasOptionsMenu(true)
        toolbar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.statusBarColor)))

        return binding.root
    }

    private fun setupMonthSlider() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

        // Generate months from January 2024 to the current month
        for (year in 2024..currentYear) {
            val endMonth = if (year == currentYear) currentMonth else 12
            for (month in 1..endMonth) {
                val monthName = "${getMonthName(month)} $year"
                monthsList.add(monthName)
            }
        }

        monthSliderAdapter = MonthAdapter(monthsList) { selectedMonthStr ->
            val (monthName, year) = selectedMonthStr.split(" ")
            val monthInt = getMonthInt(monthName)
            selectedMonth = monthInt
            selectedYear = year.toInt()

            // Show progress bar while fetching data
            binding.progressBar.visibility = View.VISIBLE

            // Fetch data after month selection
            fetchUserTokenAndFetchStatistics()
        }

        binding.monthSlider.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.monthSlider.adapter = monthSliderAdapter

        // Scroll to the current month
        binding.monthSlider.post {
            val currentMonthPosition = monthsList.indexOf(getCurrentMonth())
            if (currentMonthPosition != -1) {
                (binding.monthSlider.layoutManager as LinearLayoutManager).scrollToPosition(
                    currentMonthPosition
                )
            }
        }
    }


    private fun fetchUserTokenAndFetchStatistics() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                idToken?.let {
                    fetchMonthlyStatistics(it, selectedYear.toString(), selectedMonth.toString())
                    fetchWeeklyExpenses(it, selectedYear.toString(), selectedMonth.toString())
                }
            } else {
                Toast.makeText(context, "Failed to get user token", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchMonthlyStatistics(bearerToken: String, year: String, month: String) {
        RetrofitInstance.api.getStatistics("Bearer $bearerToken", year, month).enqueue(object :
            Callback<StatisticsResponse> {
            override fun onFailure(call: Call<StatisticsResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE  // Hide progress bar on error
            }

            override fun onResponse(
                call: Call<StatisticsResponse>,
                response: Response<StatisticsResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { statisticsResponse ->
                        val totalIncome = statisticsResponse.totalIncome
                        val totalOutcome = statisticsResponse.totalOutcome

                        if (totalIncome == 0.0 && totalOutcome == 0.0) {
                            showEmptyState()
                        } else {
                            updatePieChart(statisticsResponse)
                            updateRecommendation(totalIncome, totalOutcome)
                            hideEmptyState()
                        }
                    }
                } else {
                    Log.e("MonthlyStatistics", "Error: ${response.message()}")
                    Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE  // Hide progress bar after data is fetched
            }
        })
    }

    private fun fetchWeeklyExpenses(bearerToken: String, year: String, month: String) {
        RetrofitInstance.api.getWeeklyExpenses("Bearer $bearerToken", year, month).enqueue(object :
            Callback<WeeklyExpensesResponse> {
            override fun onFailure(call: Call<WeeklyExpensesResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE  // Hide progress bar on error
            }

            override fun onResponse(
                call: Call<WeeklyExpensesResponse>,
                response: Response<WeeklyExpensesResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { weeklyExpensesResponse ->
                        val weeklyExpenses = weeklyExpensesResponse.weeklyExpenses
                        updateBarChart(weeklyExpenses)
                    }
                } else {
                    Log.e("WeeklyExpenses", "Error: ${response.message()}")
                }
                binding.progressBar.visibility = View.GONE  // Hide progress bar after data is fetched
            }
        })
    }


    private fun updateBarChart(weeklyExpenses: List<WeeklyExpense>) {
        val entries = mutableListOf<BarEntry>()
        val labels = listOf("Week 1", "Week 2", "Week 3", "Week 4")
        val expensesMap = mutableMapOf("Week 1" to 0f, "Week 2" to 0f, "Week 3" to 0f, "Week 4" to 0f)

        // Update map with data from server
        weeklyExpenses.forEach { week ->
            val weekInt = week.week.split(" ")[1].toIntOrNull()
            if (weekInt != null && weekInt in 1..4) {
                expensesMap["Week $weekInt"] = week.totalExpense
            }
        }

        // Add data to BarChart
        expensesMap.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value))
        }

        val dataSet = BarDataSet(entries, "Weekly Expenses")
        dataSet.colors = listOf(
            Color.parseColor("#80bf94"), // Green
            Color.parseColor("#0aa9ff"), // Blue
            Color.parseColor("#db1916"), // Red
            Color.parseColor("#fbff0a")  // Yellow
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barData.barWidth = 0.3f
        barChart.data = barData

        // Load custom font
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular)

        // Customize X-axis
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setLabelRotationAngle(-45f)
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 14f
        xAxis.typeface = typeface

        // Customize legend
        val legend = barChart.legend
        legend.textColor = Color.BLACK
        legend.textSize = 14f
        legend.typeface = typeface

        // Adjust the bottom padding
        barChart.setExtraBottomOffset(30f)

        // Refresh the chart
        barChart.invalidate()
    }

    private fun updatePieChart(statisticsResponse: StatisticsResponse) {
        val totalIncome = statisticsResponse.totalIncome
        val totalOutcome = statisticsResponse.totalOutcome
        val total = totalIncome + totalOutcome
        val incomePercentage = (totalIncome / total) * 100
        val outcomePercentage = (totalOutcome / total) * 100

        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(totalIncome.toFloat(), "Income (${String.format("%.1f", incomePercentage)}%)"))
        entries.add(PieEntry(totalOutcome.toFloat(), "Outcome (${String.format("%.1f", outcomePercentage)}%)"))

        // Create PieDataSet with custom colors
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#0aa9ff"), // Blue
            Color.parseColor("#db1916")  // Red
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        // Load custom font
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular)


        // Add formatting and animations
        val pieData = PieData(dataSet)
        pieData.setValueTextSize(16f)
        pieData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.0f", value)
            }
        })
        pieData.setValueTypeface(typeface) // Set custom font for pie data

        pieChart.data = pieData
        pieChart.setDrawSliceText(false)

        val legend = pieChart.legend
        legend.isEnabled = true
        legend.textSize = 10f
        legend.formSize = 10f
        legend.xEntrySpace = 20f
        legend.typeface = typeface // Set custom font for legend

        pieChart.invalidate() // Refresh the chart
    }



    private fun updateRecommendation(totalIncome: Double, totalOutcome: Double) {
        val modelFile = modelManager.getModelFile()
        if (!modelFile.exists()) {
            Log.e("RecomendFragment", "Model file not found: ${modelFile.absolutePath}")
            Toast.makeText(context, "Model not found. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val interpreter = Interpreter(modelFile)

            // Preparing the input tensor (income and outcome)
            val inputArray = Array(1) { FloatArray(2) }
            inputArray[0][0] = totalIncome.toFloat()
            inputArray[0][1] = totalOutcome.toFloat()

            // Output tensor for predictions (change shape to [1, 1])
            val outputArray = Array(1) { FloatArray(1) }  // 2D array with shape [1, 1]

            // Run inference
            interpreter.run(inputArray, outputArray)

            // Get the prediction value from the output tensor
            val prediction = outputArray[0][0]

            // Generate recommendations based on predictions
            val recommendation = generateRecommendation(prediction, totalIncome, totalOutcome)
            recommendationText.text = recommendation

        } catch (e: Exception) {
            Log.e("RecomendFragment", "Error running model", e)
            Toast.makeText(context, "Error running the model", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateRecommendation(prediction: Float, totalIncome: Double, totalOutcome: Double): String {
        // Format the values as IDR (Indonesian Rupiah)
        val totalIncomeFormatted = "Rp ${"%.2f".format(totalIncome)}"
        val totalOutcomeFormatted = "Rp ${"%.2f".format(totalOutcome)}"

        // Calculate outcome percentage
        val total = totalIncome + totalOutcome
        val outcomePercentage = if (total > 0) (totalOutcome / total) * 100 else 0.0

        // Generate recommendations based on outcome percentage ranges
        return when {
            outcomePercentage in 0.0..9.9 -> {
                "Your expenses are quite low (Total Outcome: $totalOutcomeFormatted, Total Income: $totalIncomeFormatted). Great job! Consider saving more or investing."
            }
            outcomePercentage in 10.0..19.9 -> {
                "Expenses are moderate (Total Outcome: $totalOutcomeFormatted, Total Income: $totalIncomeFormatted). You could review and potentially reduce some costs."
            }
            outcomePercentage in 20.0..29.9 -> {
                "You are spending a significant portion of your income (Total Outcome: $totalOutcomeFormatted, Total Income: $totalIncomeFormatted). Consider cutting down on unnecessary expenses."
            }
            outcomePercentage in 30.0..39.9 -> {
                "High expenses (Total Outcome: $totalOutcomeFormatted, Total Income: $totalIncomeFormatted). It's time to look for ways to optimize your spending and increase savings."
            }
            outcomePercentage in 40.0..49.9 -> {
                "Your expenses are quite high (Total Outcome: $totalOutcomeFormatted, Total Income: $totalIncomeFormatted). Focus on budgeting and cutting back on non-essential items."
            }
            outcomePercentage >= 50.0 -> {
                "Your expenses are above 50% (Total Outcome: $totalOutcomeFormatted, Total Income: $totalIncomeFormatted). This is a red flag. It's crucial to reduce spending to avoid financial stress."
            }
            else -> {
                "Unable to calculate recommendation. Please check your data (Total Outcome: $totalOutcomeFormatted, Total Income: $totalIncomeFormatted)."
            }
        }
    }


    private fun showEmptyState() {
        tvEmptyMessage.visibility = View.VISIBLE
        pieChart.visibility = View.GONE
        barChart.visibility = View.GONE
        recommendationText.visibility = View.GONE
    }

    private fun hideEmptyState() {
        tvEmptyMessage.visibility = View.GONE
        pieChart.visibility = View.VISIBLE
        barChart.visibility = View.VISIBLE
        recommendationText.visibility = View.VISIBLE
    }

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

    private fun getCurrentMonth(): String {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return "${getMonthName(currentMonth)} $currentYear"
    }
}
