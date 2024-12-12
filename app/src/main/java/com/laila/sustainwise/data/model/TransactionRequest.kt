package com.laila.sustainwise.data.model

data class TransactionRequest(
    val type: String,
    val category: String,
    val amount: String,
    val date: String
)
