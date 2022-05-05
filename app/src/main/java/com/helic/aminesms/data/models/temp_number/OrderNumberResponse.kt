package com.helic.aminesms.data.models.temp_number


import com.google.gson.annotations.SerializedName
import com.helic.aminesms.data.models.number_data.TempNumberData

data class OrderNumberResponse(
    @SerializedName("data")
    val tempNumberData: TempNumberData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)