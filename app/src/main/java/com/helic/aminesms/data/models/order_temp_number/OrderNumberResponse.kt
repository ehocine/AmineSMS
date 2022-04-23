package com.helic.aminesms.data.models.order_temp_number


import com.google.gson.annotations.SerializedName
import com.helic.aminesms.data.models.number_data.NumberData

data class OrderNumberResponse(
    @SerializedName("data")
    val numberData: NumberData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)