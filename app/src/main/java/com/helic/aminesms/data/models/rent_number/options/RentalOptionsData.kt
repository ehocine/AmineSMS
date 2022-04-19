package com.helic.aminesms.data.models.rent_number.options


import com.google.gson.annotations.SerializedName

data class RentalOptionsData(
    @SerializedName("duration")
    val duration: Duration = Duration(),
    @SerializedName("rentalType")
    val rentalType: String = ""
)