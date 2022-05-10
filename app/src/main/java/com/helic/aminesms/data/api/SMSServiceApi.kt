package com.helic.aminesms.data.api

import com.helic.aminesms.data.models.UserBalanceResponse
import com.helic.aminesms.data.models.cancel_number.CancelNumberResponse
import com.helic.aminesms.data.models.messages.rental_numbers_messages.RentalNumbersMessagesResponse
import com.helic.aminesms.data.models.messages.temp_numbers_messages.MessageResponse
import com.helic.aminesms.data.models.rental_numbers.RentalNumberServiceStateListResponse
import com.helic.aminesms.data.models.rental_numbers.activate_number.ActivateNumberResponse
import com.helic.aminesms.data.models.rental_numbers.live_rental_numbers.LiveRentalNumbersResponse
import com.helic.aminesms.data.models.rental_numbers.order_rental_number.OrderRentalNumberResponse
import com.helic.aminesms.data.models.rental_numbers.pending_rental_numbers.PendingRentalNumbersResponse
import com.helic.aminesms.data.models.rental_numbers.refund_rental_number.RefundRentalNumberResponse
import com.helic.aminesms.data.models.rental_numbers.renew_rental_number.RenewRentalNumberResponse
import com.helic.aminesms.data.models.rental_numbers.rental_options.RentalOptionsResponse
import com.helic.aminesms.data.models.rental_numbers.rental_service_price.RentalServicePrice
import com.helic.aminesms.data.models.temp_number.OrderNumberResponse
import com.helic.aminesms.data.models.temp_number.reusable_numbers.ReusableNumbersResponse
import com.helic.aminesms.data.models.temp_number.reusable_numbers.reuse_number_details.ReuseNumberResponse
import com.helic.aminesms.data.models.temp_number.service_state.ServiceStateListResponse
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
    suspend fun getRentalServiceStateList(): Response<RentalNumberServiceStateListResponse>

    @GET("/api/v2/rentals/options")
    suspend fun getRentalOptions(): Response<RentalOptionsResponse>

    @GET("/api/v2/rentals/price")
    suspend fun getRentalServicePrice(
        @Query("durationInHours") durationInHours: Int,
        @Query("serviceId") serviceId: String
    ): Response<RentalServicePrice>

    @FormUrlEncoded
    @POST("/api/v2/rentals")
    suspend fun orderRentalNumber(
        @Field("serviceId") serviceId: String,
        @Field("durationInHours") durationInHours: Int,
    ): Response<OrderRentalNumberResponse>


    @GET("/api/v2/rentals/{rentalId}/messages")
    suspend fun getRentalNumberMessages(@Path("rentalId") rentalId: String): Response<RentalNumbersMessagesResponse>

    @GET("/api/v2/rentals/live")
    suspend fun getLiveRentalNumbersList(): Response<LiveRentalNumbersResponse>

    @GET("/api/v2/rentals/pending")
    suspend fun getPendingRentalNumbersList(): Response<PendingRentalNumbersResponse>

    @POST("/api/v2/rentals/{rentalId}/activate")
    suspend fun activateRentalNumber(@Path("rentalId") rentalId: String): Response<ActivateNumberResponse>

    @POST("/api/v2/rentals/{rentalId}/refund")
    suspend fun requestRefundRentalNumber(@Path("rentalId") rentalId: String): Response<RefundRentalNumberResponse>

    @POST("/api/v2/rentals/{rentalId}/renew")
    suspend fun renewRentalNumber(@Path("rentalId") rentalId: String): Response<RenewRentalNumberResponse>
}