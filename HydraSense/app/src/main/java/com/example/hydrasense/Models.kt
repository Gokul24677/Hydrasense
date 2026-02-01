package com.example.hydrasense

data class AddReadingRequest(
    val user_id: String,
    val ph: Double,
    val time: String? = null,
    val password: String? = "admin123",
    val ph_value: Double? = null // For backward compatibility
)

data class LoginRequest(
    val user_id: String,
    val password: String
)

data class MessageResponse(
    val message: String,
    val error: String? = null
)

data class NetworkReading(
    val user_id: String,
    val password: String,
    val ph_value: Double,
    val timestamp: String,
    val date: String
)

data class DiscoveryResponse(
    val status: String,
    val device_count: Int,
    val devices: List<DeviceItem>
)

data class DeviceItem(
    val ip: String,
    val mac: String
)
