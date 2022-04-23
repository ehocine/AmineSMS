package com.helic.aminesms.data.models.reusable_numbers.reuse_number_details


import com.google.gson.annotations.SerializedName
import com.helic.aminesms.data.models.number_data.NumberData

data class ReuseNumberResponse(
    @SerializedName("data")
    val reuseNumberData: NumberData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)