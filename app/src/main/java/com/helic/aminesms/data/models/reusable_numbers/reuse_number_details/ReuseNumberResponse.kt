package com.helic.aminesms.data.models.reusable_numbers.reuse_number_details


import com.google.gson.annotations.SerializedName

data class ReuseNumberResponse(
    @SerializedName("data")
    val reuseNumberData: ReuseNumberData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)