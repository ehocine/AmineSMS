package com.helic.aminesms.data.models.rent_number.options


import com.google.gson.annotations.SerializedName

data class RentalOptionsResponse(
    @SerializedName("data")
    val data: List<Data>,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)