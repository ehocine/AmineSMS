package com.helic.aminesms.data.api

import com.helic.aminesms.data.models.UserBalanceResponse
import com.helic.aminesms.data.models.cancel_number.CancelNumberResponse
import com.helic.aminesms.data.models.messages.MessageResponse
import com.helic.aminesms.data.models.order_number.OrderNumberResponse
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
    suspend fun checkForMessages(@Path("temporaryNumberId") temporaryNumberId: String): Response<MessageResponse>

    @POST("/api/v2/temporary/{temporaryNumberId}/cancel")
    suspend fun cancelTempNumber(@Path("temporaryNumberId") temporaryNumberId: String): Response<CancelNumberResponse>

    @GET("/api/v2/balance")
    suspend fun getSuperUserBalance(): Response<UserBalanceResponse>

}