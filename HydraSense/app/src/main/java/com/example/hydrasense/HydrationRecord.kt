package com.example.hydrasense

data class HydrationRecord(
    val time: String,
    val recommendation: String,
    val ph: String,
    val status: String
)
