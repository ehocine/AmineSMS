package com.helic.aminesms.data.models.rental_numbers.activate_number


import com.google.gson.annotations.SerializedName

data class ActivateNumberResponse(
    @SerializedName("data")
    val activateRentalNumberData: ActivateRentalNumberData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)