package com.helic.aminesms.data.models.reusable_numbers


import com.google.gson.annotations.SerializedName

data class ReusableNumbersResponse(
    @SerializedName("data")
    val reusableNumbersListData: List<ReusableNumbersData>,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)