package com.helic.aminesms.data.models.rent_number


import com.google.gson.annotations.SerializedName

data class RentNumberServiceState(
    @SerializedName("isAvailable")
    val isAvailable: Boolean,
    @SerializedName("name")
    val name: String,
    @SerializedName("serviceId")
    val serviceId: String
)