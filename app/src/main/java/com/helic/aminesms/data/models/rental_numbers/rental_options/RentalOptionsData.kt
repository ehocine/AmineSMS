package com.helic.aminesms.data.models.rental_numbers.rental_options

import com.google.gson.annotations.SerializedName

data class RentalOptionsData(
    @SerializedName("duration")
    val duration: Duration = Duration(),
    @SerializedName("rentalType")
    val rentalType: String = ""
)
