package com.helic.aminesms.data.models.rental_numbers.renewal_rental_numbers


import com.google.gson.annotations.SerializedName

data class RenewalRequestStateResponse(
    @SerializedName("data")
    val renewalRequestStateData: RenewalRequestStateData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)