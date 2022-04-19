package com.helic.aminesms.data.models.service_state


import com.google.gson.annotations.SerializedName

data class ServiceState(
    @SerializedName("isAvailable")
    val isAvailable: Boolean = false,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("serviceId")
    val serviceId: String = ""
)