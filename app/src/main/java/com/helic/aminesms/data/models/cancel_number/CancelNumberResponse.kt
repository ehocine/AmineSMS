package com.helic.aminesms.data.models.cancel_number


import com.google.gson.annotations.SerializedName

data class CancelNumberResponse(
    @SerializedName("errors")
    val errors: String = "",
    @SerializedName("msg")
    val msg: String = "",
    @SerializedName("succeeded")
    val succeeded: Boolean = false
)