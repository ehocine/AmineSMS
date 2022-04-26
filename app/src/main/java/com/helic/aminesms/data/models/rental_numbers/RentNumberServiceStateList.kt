package com.helic.aminesms.data.models.rental_numbers

import com.google.gson.annotations.SerializedName

data class RentNumberServiceStateList(
    @SerializedName("data")
    val rentalServiceStateList: List<RentNumberServiceState>,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)
