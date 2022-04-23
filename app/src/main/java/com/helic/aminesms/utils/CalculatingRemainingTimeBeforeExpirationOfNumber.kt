package com.helic.aminesms.utils

import android.content.Context
import androidx.compose.material.SnackbarDuration
import com.helic.aminesms.data.models.order_number.OrderedNumberData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun calculatingRemainingExpirationTime(
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
    } else {
        difference = 0
    }
    return difference
}

fun calculatingRemainingTime(
    orderedNumberData: OrderedNumberData
): Int {
    val localDate = LocalDate.now()
    val startOfDay: LocalDateTime = localDate.atTime(LocalTime.now())
    val timestamp = startOfDay.atZone(ZoneId.systemDefault()).toInstant().epochSecond

    var difference = orderedNumberData.expiresAt - timestamp.toInt()

    if (difference <= 0) difference = 0
    return difference
}

fun convertSeconds(seconds: Int): String {

    val numberOfHours = (seconds % 86400) / 3600
    val numberOfMinutes = ((seconds % 86400) % 3600) / 60
    val numberOfSeconds = ((seconds % 86400) % 3600) % 60

    val hoursText = when {
        numberOfHours < 10 -> "0$numberOfHours"
        numberOfHours == 0 -> "00"
        else -> numberOfHours
    }

    val minutesText = when {
        numberOfMinutes < 10 -> "0$numberOfMinutes"
        numberOfMinutes == 0 -> "00"
        else -> numberOfMinutes
    }

    val secondsText = when {
        numberOfSeconds < 10 -> "0$numberOfSeconds"
        numberOfSeconds == 0 -> "00"
        else -> numberOfSeconds
    }
    return "$hoursText:$minutesText:$secondsText"
}