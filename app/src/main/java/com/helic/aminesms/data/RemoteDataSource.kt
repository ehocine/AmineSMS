package com.helic.aminesms.data

import com.helic.aminesms.data.api.SMSServiceApi
import com.helic.aminesms.data.models.UserBalanceResponse
import com.helic.aminesms.data.models.cancel_number.CancelNumberResponse
import com.helic.aminesms.data.models.messages.MessageResponse
import com.helic.aminesms.data.models.order_temp_number.OrderNumberResponse
import com.helic.aminesms.data.models.rental_numbers.RentalNumberServiceStateListResponse
import com.helic.aminesms.data.models.rental_numbers.rental_options.RentalOptionsResponse
import com.helic.aminesms.data.models.rental_numbers.rental_service_price.RentalServicePrice
import com.helic.aminesms.data.models.reusable_numbers.ReusableNumbersResponse
import com.helic.aminesms.data.models.reusable_numbers.reuse_number_details.ReuseNumberResponse
import com.helic.aminesms.data.models.service_state.ServiceStateListResponse
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val smsServiceApi: SMSServiceApi) {

    suspend fun getServiceStateList(): Response<ServiceStateListResponse> {
        return smsServiceApi.getServiceStateList()
    }

    suspend fun orderNumber(
        serviceID: String,
        areaCode: String,
    ): Response<OrderNumberResponse> {
        return smsServiceApi.orderNumber(
            serviceID = serviceID,
            areaCode = areaCode
        )
    }

    suspend fun getTempNumberInfo(temporaryNumberId: String): Response<MessageResponse> {
        return smsServiceApi.getTempNumberInfo(temporaryNumberId = temporaryNumberId)
    }


    suspend fun cancelTempNumber(temporaryNumberId: String): Response<CancelNumberResponse> {
        return smsServiceApi.cancelTempNumber(temporaryNumberId = temporaryNumberId)
    }

    suspend fun getSuperUserBalance(): Response<UserBalanceResponse> {
        return smsServiceApi.getSuperUserBalance()
    }

    suspend fun getReusableNumbers(): Response<ReusableNumbersResponse> {
        return smsServiceApi.getReusableNumbers()
    }

    suspend fun reuseNumber(temporaryNumberId: String): Response<ReuseNumberResponse> {
        return smsServiceApi.reuseNumber(temporaryNumberId = temporaryNumberId)
    }

    suspend fun getRentNumberServiceStateList(): Response<RentalNumberServiceStateListResponse> {
        return smsServiceApi.getRentalServiceStateList()
    }

    suspend fun getRentalOptions(): Response<RentalOptionsResponse> {
        return smsServiceApi.getRentalOptions()
    }

    suspend fun getRentalServicePrice(
        durationInHours: Int,
        serviceId: String
    ): Response<RentalServicePrice> {
        return smsServiceApi.getRentalServicePrice(
            durationInHours = durationInHours,
            serviceId = serviceId
        )
    }
}