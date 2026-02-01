package com.example.hydrasense

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("add_reading")
    fun addReading(@Body request: AddReadingRequest): Call<MessageResponse>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<MessageResponse>

    @POST("register")
    fun register(@Body request: LoginRequest): Call<MessageResponse>

    @POST("update_profile")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<MessageResponse>

    @GET("get_readings")
    fun getReadings(@Query("user_id") userId: String): Call<List<NetworkReading>>

    @GET("discover_devices")
    fun discoverDevices(): Call<DiscoveryResponse>

    @POST("send_otp")
    fun sendOtp(@Body request: OtpRequest): Call<MessageResponse>

    @POST("verify_otp")
    fun verifyOtp(@Body request: OtpVerifyRequest): Call<MessageResponse>
}

data class OtpRequest(val email: String)
data class OtpVerifyRequest(val email: String, val code: String)

data class UpdateProfileRequest(
    val user_id: String,
    val gender: String,
    val dob: String,
    val weight: Double,
    val height: Double,
    val daily_goal: Double
)
