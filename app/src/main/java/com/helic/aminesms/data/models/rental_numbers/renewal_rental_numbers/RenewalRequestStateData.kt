package com.helic.aminesms.data.models.rental_numbers.renewal_rental_numbers


import com.google.gson.annotations.SerializedName

data class RenewalRequestStateData(
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("renewalId")
    val renewalId: String = "",
    @SerializedName("state")
    val state: String = ""
)