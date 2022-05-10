package com.helic.aminesms.data.models.rental_numbers.activate_number


import com.google.gson.annotations.SerializedName

data class ActivateRentalNumberData(
    @SerializedName("rentalActiveForSeconds")
    val rentalActiveForSeconds: Int = 0,
    @SerializedName("timeUntilActivatedEstimateSeconds")
    val timeUntilActivatedEstimateSeconds: Int = 0
)