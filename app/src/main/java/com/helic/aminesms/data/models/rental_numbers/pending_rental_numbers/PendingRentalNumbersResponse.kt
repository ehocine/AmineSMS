package com.helic.aminesms.data.models.rental_numbers.pending_rental_numbers


import com.google.gson.annotations.SerializedName
import com.helic.aminesms.data.models.number_data.RentalNumberData

data class PendingRentalNumbersResponse(
    @SerializedName("data")
    val rentalNumberDataList: MutableList<RentalNumberData>,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)