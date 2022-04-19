package com.helic.aminesms.data.models.rent_number.options


import com.google.gson.annotations.SerializedName

data class Duration(
    @SerializedName("days")
    val days: Int,
    @SerializedName("hours")
    val hours: Int,
    @SerializedName("milliseconds")
    val milliseconds: Int,
    @SerializedName("minutes")
    val minutes: Int,
    @SerializedName("seconds")
    val seconds: Int,
    @SerializedName("ticks")
    val ticks: Int,
    @SerializedName("totalDays")
    val totalDays: Int,
    @SerializedName("totalHours")
    val totalHours: Int,
    @SerializedName("totalMilliseconds")
    val totalMilliseconds: Int,
    @SerializedName("totalMinutes")
    val totalMinutes: Int,
    @SerializedName("totalSeconds")
    val totalSeconds: Int
)