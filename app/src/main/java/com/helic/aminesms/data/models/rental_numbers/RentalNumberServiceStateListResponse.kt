package com.helic.aminesms.data.models.rental_numbers

import com.google.gson.annotations.SerializedName

data class RentalNumberServiceStateListResponse(
    @SerializedName("data")
    val rentalServiceStateList: List<RentalNumberServiceState>,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)
