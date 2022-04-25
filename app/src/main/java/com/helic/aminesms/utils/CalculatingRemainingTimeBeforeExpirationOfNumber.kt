package com.helic.aminesms.utils

import android.content.Context
import androidx.compose.material.SnackbarDuration
import com.helic.aminesms.data.models.number_data.NumberData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun calculatingRemainingExpirationTime(
    context: Context,
    numberData: NumberData,
    snackbar: (String, SnackbarDuration) -> Unit,
    userBalance: Double
): Int {

    val localDate = LocalDate.now()
    val startOfDay: LocalDateTime = localDate.atTime(LocalTime.now())
    val timestamp = startOfDay.atZone(ZoneId.systemDefault()).toInstant().epochSecond

    var difference = numberData.expiresAt - timestamp.toInt()

    if (numberData.state == NumberState.Pending.toString()) {

        if (difference <= 0) {
            updateNumberState(
                context = context,
                snackbar = snackbar,
                numberToBeUpdated = numberData,
                NumberState.Expired
            )
            handleOrderedNumberState(
                context = context,
                snackbar,
                NumberState.Expired,
                userBalance,
                numberData.price
            )
            difference = 0
        }
    } else {
        difference = 0
    }
    return difference
}

fun calculatingRemainingReuseTime(
    numberData: NumberData
): Int {
    val localDate = LocalDate.now()
    val startOfDay: LocalDateTime = localDate.atTime(LocalTime.now())
    val timestamp = startOfDay.atZone(ZoneId.systemDefault()).toInstant().epochSecond

    var difference = numberData.reuseableUntil - timestamp.toInt()
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