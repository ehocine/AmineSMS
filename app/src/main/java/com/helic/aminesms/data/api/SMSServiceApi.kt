package com.helic.aminesms.data.api

import com.helic.aminesms.data.models.UserBalanceResponse
import com.helic.aminesms.data.models.cancel_number.CancelNumberResponse
import com.helic.aminesms.data.models.messages.MessageResponse
import com.helic.aminesms.data.models.order_temp_number.OrderNumberResponse
import com.helic.aminesms.data.models.rental_numbers.RentNumberServiceStateList
import com.helic.aminesms.data.models.rental_numbers.rental_options.RentalOptionsResponse
import com.helic.aminesms.data.models.reusable_numbers.ReusableNumbersResponse
import com.helic.aminesms.data.models.reusable_numbers.reuse_number_details.ReuseNumberResponse
import com.helic.aminesms.data.models.service_state.ServiceStateListResponse
import retrofit2.Response
import retrofit2.http.*

interface SMSServiceApi {

    @GET("/api/v2/temporary/services")
    suspend fun getServiceStateList(): Response<ServiceStateListResponse>

    @FormUrlEncoded
    @POST("/api/v2/temporary")
    suspend fun orderNumber(
        @Field("serviceId") serviceID: String,
        @Field("areaCode") areaCode: String,
    ): Response<OrderNumberResponse>

    @GET("/api/v2/temporary/{temporaryNumberId}")
    suspend fun getTempNumberInfo(@Path("temporaryNumberId") temporaryNumberId: String): Response<MessageResponse>

    @POST("/api/v2/temporary/{temporaryNumberId}/cancel")
    suspend fun cancelTempNumber(@Path("temporaryNumberId") temporaryNumberId: String): Response<CancelNumberResponse>

    @GET("/api/v2/balance")
    suspend fun getSuperUserBalance(): Response<UserBalanceResponse>

    @GET("/api/v2/temporary/reusable")
    suspend fun getReusableNumbers(): Response<ReusableNumbersResponse>

    @POST("/api/v2/temporary/reusable/{temporaryNumberId}")
    suspend fun reuseNumber(@Path("temporaryNumberId") temporaryNumberId: String): Response<ReuseNumberResponse>

    @GET("/api/v2/rentals/services")
    suspend fun getRentalServiceStateList(): Response<RentNumberServiceStateList>

    @GET("/api/v2/rentals/options")
    suspend fun getRentalOptions(): Response<RentalOptionsResponse>

}