package com.helic.aminesms.utils

import android.content.Context
import android.util.Log
import androidx.compose.material.SnackbarDuration
import com.helic.aminesms.data.models.order_number.OrderedNumberData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun calculatingRemainingTime(
    context: Context,
    orderedNumberData: OrderedNumberData,
    snackbar: (String, SnackbarDuration) -> Unit,
    userBalance: Double
): Int {

    val localDate = LocalDate.now()
    val startOfDay: LocalDateTime = localDate.atTime(LocalTime.now())
    val timestamp = startOfDay.atZone(ZoneId.systemDefault()).toInstant().epochSecond

    var difference = orderedNumberData.expiresAt - timestamp.toInt()

    if (orderedNumberData.state == NumberState.Pending.toString()) {

        if (difference <= 0) {
            updateNumberState(
                context = context,
                snackbar = snackbar,
                numberToBeUpdated = orderedNumberData,
                NumberState.Expired
            )
            handleOrderedNumberState(
                context = context,
                snackbar,
                NumberState.Expired,
                userBalance,
                orderedNumberData.price
            )
            difference = 0
        }
    }else{
        difference = 0
    }
    return difference

}