package com.helic.aminesms.data.models.rent_number.options


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("duration")
    val duration: Duration,
    @SerializedName("rentalType")
    val rentalType: String
)