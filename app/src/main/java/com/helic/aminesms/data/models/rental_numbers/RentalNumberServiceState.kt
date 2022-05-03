package com.helic.aminesms.data.models.rental_numbers

import com.google.gson.annotations.SerializedName

data class RentalNumberServiceState(
    @SerializedName("isAvailable")
    val isAvailable: Boolean = false,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("serviceId")
    val serviceId: String = ""
)
