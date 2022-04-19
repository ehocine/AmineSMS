package com.helic.aminesms.data.models.order_number


import com.google.gson.annotations.SerializedName

data class OrderNumberResponse(
    @SerializedName("data")
    val orderedNumberData: OrderedNumberData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)