package com.helic.aminesms.data

import com.helic.aminesms.data.api.SMSServiceApi
import com.helic.aminesms.data.models.UserBalanceResponse
import com.helic.aminesms.data.models.cancel_number.CancelNumberResponse
import com.helic.aminesms.data.models.messages.MessageResponse
import com.helic.aminesms.data.models.order_number.OrderNumberResponse
import com.helic.aminesms.data.models.rent_number.RentNumberServiceStateList
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

    suspend fun checkForMessages(temporaryNumberId: String): Response<MessageResponse> {
        return smsServiceApi.checkForMessages(temporaryNumberId = temporaryNumberId)
    }


    suspend fun cancelTempNumber(temporaryNumberId: String): Response<CancelNumberResponse> {
        return smsServiceApi.cancelTempNumber(temporaryNumberId = temporaryNumberId)
    }

    suspend fun getSuperUserBalance(): Response<UserBalanceResponse> {
        return smsServiceApi.getSuperUserBalance()
    }

    suspend fun getRentNumberServiceStateList(): Response<RentNumberServiceStateList> {
        return smsServiceApi.getRentalServiceStateList()
    }
}