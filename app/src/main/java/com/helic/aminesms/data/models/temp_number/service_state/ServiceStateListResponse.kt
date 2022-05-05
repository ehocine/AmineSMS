package com.helic.aminesms.data.models.temp_number.service_state

import com.google.gson.annotations.SerializedName

data class ServiceStateListResponse(
    @SerializedName("data")
    val serviceStateList: List<ServiceState>,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean

)