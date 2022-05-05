package com.helic.aminesms.data.models.temp_number.reusable_numbers.reuse_number_details


import com.google.gson.annotations.SerializedName
import com.helic.aminesms.data.models.number_data.TempNumberData

data class ReuseNumberResponse(
    @SerializedName("data")
    val reuseTempNumberData: TempNumberData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)