package com.helic.aminesms.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material.SnackbarDuration
import com.helic.aminesms.data.models.number_data.TempNumberData
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt


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

//    var difference = tempNumberData.reuseableUntil - timestamp.toInt()
    var difference = tempNumberData.expiresAt - timestamp.toInt() // For testing

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

@SuppressLint("SimpleDateFormat")
fun convertTimeStampToDate(epoch: Long): String {
    val date = Date(epoch * 1000L)
    val sdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm aaa")
    return sdf.format(date)
}

fun calculatingRemainingDaysForRentals(epoch: Long): Double {
    val localDate = LocalDate.now()
    val startOfDay: LocalDateTime = localDate.atTime(LocalTime.now())
    val timestamp = startOfDay.atZone(ZoneId.systemDefault()).toInstant().epochSecond

    val seconds = (epoch - timestamp).toDouble()
    val minutes = seconds / 60
    val hours = minutes / 60

    val number3digits: Double = (hours / 24 * 1000.0).roundToInt() / 1000.0
    val number2digits: Double = (number3digits * 100.0).roundToInt() / 100.0
    val remainingTimeInHours = (number2digits * 10.0).roundToInt() / 10.0 // One decimal
    return if (remainingTimeInHours < 0) 0.0 else remainingTimeInHours
}