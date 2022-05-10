package com.helic.aminesms.data.models.rental_numbers.refund_rental_number


import com.google.gson.annotations.SerializedName

data class RefundRentalNumberResponse(
    @SerializedName("data")
    val data: Boolean,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)