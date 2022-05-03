package com.helic.aminesms.data.models.rental_numbers.rental_options

import com.google.gson.annotations.SerializedName

data class Duration(
    @SerializedName("days")
    val days: Int = 0,
    @SerializedName("hours")
    val hours: Int = 0,
    @SerializedName("milliseconds")
    val milliseconds: Int = 0,
    @SerializedName("minutes")
    val minutes: Int = 0,
    @SerializedName("seconds")
    val seconds: Int = 0,
    @SerializedName("ticks")
    val ticks: Int = 0,
    @SerializedName("totalDays")
    val totalDays: Double = 0.0,
    @SerializedName("totalHours")
    val totalHours: Double = 0.0,
    @SerializedName("totalMilliseconds")
    val totalMilliseconds: Double = 0.0,
    @SerializedName("totalMinutes")
    val totalMinutes: Double = 0.0,
    @SerializedName("totalSeconds")
    val totalSeconds: Double = 0.0
)
