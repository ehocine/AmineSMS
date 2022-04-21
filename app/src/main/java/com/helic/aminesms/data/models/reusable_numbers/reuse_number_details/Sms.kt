package com.helic.aminesms.data.models.reusable_numbers.reuse_number_details


import com.google.gson.annotations.SerializedName

data class Sms(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("content")
    val content: String = "",
    @SerializedName("createdAt")
    val createdAt: Int = 0,
    @SerializedName("parentObjectId")
    val parentObjectId: String = "",
    @SerializedName("sender")
    val sender: String = ""
)