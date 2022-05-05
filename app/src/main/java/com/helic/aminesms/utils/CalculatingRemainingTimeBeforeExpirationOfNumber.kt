package com.helic.aminesms.utils

import android.content.Context
import androidx.compose.material.SnackbarDuration
import com.helic.aminesms.data.models.number_data.TempNumberData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun calculatingRemainingExpirationTime(
    context: Context,
    tempNumberData: TempNumberData,
    snackbar: (String, SnackbarDuration) -> Unit,
    userBalance: Double
): Int {

    val localDate = LocalDate.now()
    val startOfDay: LocalDateTime = localDate.atTime(LocalTime.now())
    val timestamp = startOfDay.atZone(ZoneId.systemDefault()).toInstant().epochSecond

    var difference = tempNumberData.expiresAt - timestamp.toInt()

    if (tempNumberData.state == NumberState.Pending.toString()) {

        if (difference <= 0) {
            updateTempNumberState(
                context = context,
                snackbar = snackbar,
                tempNumberToBeUpdated = tempNumberData,
                NumberState.Expired
            )
            handleOrderedNumberState(
                context = context,
                snackbar,
                NumberState.Expired,
                userBalance,
                tempNumberData.price
            )
            difference = 0
        }
    } else {
        difference = 0
    }
    return difference
}

fun calculatingRemainingReuseTime(
    tempNumberData: TempNumberData
): Int {
    val localDate = LocalDate.now()
    val startOfDay: LocalDateTime = localDate.atTime(LocalTime.now())
    val timestamp = startOfDay.atZone(ZoneId.systemDefault()).toInstant().epochSecond

    var difference = tempNumberData.reuseableUntil - timestamp.toInt()
//    var difference = numberData.expiresAt - timestamp.toInt() // For testing

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