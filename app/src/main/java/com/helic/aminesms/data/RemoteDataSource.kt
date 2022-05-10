package com.helic.aminesms.data

import com.helic.aminesms.data.api.SMSServiceApi
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

    suspend fun orderRentalNumber(
        serviceId: String,
        durationInHours: Int
    ): Response<OrderRentalNumberResponse> {
        return smsServiceApi.orderRentalNumber(
            serviceId = serviceId,
            durationInHours = durationInHours
        )
    }

    suspend fun getRentalNumberMessages(rentalId: String): Response<RentalNumbersMessagesResponse> {
        return smsServiceApi.getRentalNumberMessages(rentalId = rentalId)
    }

    suspend fun getLiveRentalNumbersList(): Response<LiveRentalNumbersResponse> {
        return smsServiceApi.getLiveRentalNumbersList()
    }

    suspend fun getPendingRentalNumbersList(): Response<PendingRentalNumbersResponse> {
        return smsServiceApi.getPendingRentalNumbersList()
    }

    suspend fun activateRentalNumber(rentalId: String): Response<ActivateNumberResponse> {
        return smsServiceApi.activateRentalNumber(rentalId = rentalId)
    }

    suspend fun requestRefundRentalNumber(rentalId: String): Response<RefundRentalNumberResponse> {
        return smsServiceApi.requestRefundRentalNumber(rentalId = rentalId)
    }

    suspend fun renewRentalNumber(rentalId: String): Response<RenewRentalNumberResponse> {
        return smsServiceApi.renewRentalNumber(rentalId = rentalId)
    }
}