package com.helic.aminesms.data.models.rental_numbers.rental_options

import com.google.gson.annotations.SerializedName

data class RentalOptionsResponse(
    @SerializedName("data")
    val rentalOptionsData: List<RentalOptionsData>,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)
