package com.helic.aminesms.data.models.messages


import com.google.gson.annotations.SerializedName

data class MessageResponse(
    @SerializedName("data")
    val data: Data,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)