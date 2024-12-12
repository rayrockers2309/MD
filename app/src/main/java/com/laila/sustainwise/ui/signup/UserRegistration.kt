package com.laila.sustainwise.ui.signup

data class HistoryItem(
    val id: String,
    val amount: String,
    val category: String,
    val type: String,
    val date: String
)

data class SaldoResponse(
    val message: String,
    val saldo: Int
)

data class EditUserRequest(
    val username: String? = null,
    val photo: String? = null
)

data class EditUserResponse(
    val message: String,
    val updatedFields: Map<String, Any>
)

data class TransactionItem(
    val id: String,
    val amount: String,
    val category: String,
    val type: String,
    val date: String
)

data class UserProfileResponse(
    val email: String,
    val username: String,
    val photo: String
)

data class WeeklyExpensesResponse(
    val message: String,
    val weeklyExpenses: List<WeeklyExpense>
)

data class WeeklyExpense(
    val week: String,
    val totalExpense: Float
)

data class StatisticsResponse(
    val message: String,
    val totalIncome: Double,
    val totalOutcome: Double
)

data class CategoryStatisticsResponse(
    val message: String,
    val totalOutcome: Float,
    val categories: Map<String, Float> // Adjusted to a Map
)

data class CategoryStat(
    val category: String,
    val totalOutcome: Float
)


data class Transaction(
    val type: String,
    val category: String,
    val amount: String,
    val date: String
)

data class DeleteTransactionResponse(
    val message: String,
    val updatedSaldo: Float
)

data class DeletePhotoResponse(
    val message: String
)

