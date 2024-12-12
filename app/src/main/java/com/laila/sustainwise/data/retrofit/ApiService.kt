package com.laila.sustainwise.data.retrofit

import com.laila.sustainwise.data.model.TransactionRequest
import com.laila.sustainwise.ui.signup.CategoryStatisticsResponse
import com.laila.sustainwise.ui.signup.DeletePhotoResponse
import com.laila.sustainwise.ui.signup.DeleteTransactionResponse
import com.laila.sustainwise.ui.signup.EditUserRequest
import com.laila.sustainwise.ui.signup.EditUserResponse
import com.laila.sustainwise.ui.signup.HistoryItem
import com.laila.sustainwise.ui.signup.SaldoResponse
import com.laila.sustainwise.ui.signup.StatisticsResponse
import com.laila.sustainwise.ui.signup.TransactionItem
import com.laila.sustainwise.ui.signup.UserProfileResponse
import com.laila.sustainwise.ui.signup.WeeklyExpensesResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {


    @GET("/transactions/monthly")
    fun getTransactions(
        @Header("Authorization") idToken: String, // Pass the user token
        @Query("type") type: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Call<ApiResponse>

    @POST("/transaction")
    fun submitTransaction(
        @Body transaction: TransactionRequest,
        @Header("Authorization") authToken: String
    ): Call<TransaksiResponse>

    @DELETE("transaction/{type}/{transactionId}")
    fun deleteTransaction(
        @Header("Authorization") idToken: String,
        @Path("type") type: String,
        @Path("transactionId") transactionId: String
    ): Call<DeleteTransactionResponse>

    @GET("/user")
    fun getUserProfile(@Header("Authorization") idToken: String): Call<UserProfileResponse>

    @GET("/transaction/weekly-expenses")
    fun getWeeklyExpenses(
        @Header("Authorization") bearerToken: String,
        @Query("year") year: String,
        @Query("month") month: String
    ): Call<WeeklyExpensesResponse>

    @GET("/transactions/latest")
    fun getLatestTransactions(
        @Header("Authorization") authToken: String
    ): Call<TransactionResponse>

    @GET("/saldo")
    fun getSaldo(
        @Header("Authorization") token: String
    ): Call<SaldoResponse>

    @PATCH("/edit-user")
    fun editUser(
        @Header("Authorization") token: String, // Header untuk ID Token
        @Body request: EditUserRequest          // Body dengan username atau photo
    ): Call<EditUserResponse>

    @GET("/statistics/income-vs-outcome")
    fun getStatistics(
        @Header("Authorization") token: String,
        @Query("year") year: String,
        @Query("month") month: String
    ): Call<StatisticsResponse>

    @GET("/statistics/outcome-by-category")
    fun getCategoryStatistics(
        @Header("Authorization") bearerToken: String,
        @Query("year") year: String,
        @Query("month") month: String
    ): Call<CategoryStatisticsResponse>

    @DELETE("/delete-photo")
    fun deleteProfilePhoto(@Header("Authorization") idToken: String): Call<DeletePhotoResponse>

}

// Response utk data-data class

data class ApiResponse(
    val message: String,
    val transactions: List<HistoryItem>
)

data class TransactionResponse(
    val message: String,
    val transactions: List<TransactionItem>
)


data class TransaksiResponse(
    val message: String
)
