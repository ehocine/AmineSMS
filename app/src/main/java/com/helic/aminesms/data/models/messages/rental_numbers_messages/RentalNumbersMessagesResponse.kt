package com.helic.aminesms.data.models.messages.rental_numbers_messages


import com.google.gson.annotations.SerializedName
import com.helic.aminesms.data.models.messages.Sms

data class RentalNumbersMessagesResponse(
    @SerializedName("data")
    val data: List<Sms>,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)