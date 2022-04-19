package com.helic.aminesms.data.models


import com.google.gson.annotations.SerializedName

data class UserBalanceResponse(
    @SerializedName("data")
    val data: Double = 0.0,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)