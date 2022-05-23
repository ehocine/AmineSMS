package com.helic.aminesms.presentation.screens.main_app_screens.messages.message_details.rental_numbers_messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helic.aminesms.data.models.messages.Sms
import com.helic.aminesms.presentation.ui.theme.MediumGray
import com.helic.aminesms.presentation.ui.theme.TextColor
import com.helic.aminesms.utils.convertTimeStampToDate

@Composable
fun MessageDetailItem(listOfMessages: List<Sms?>) {
    DisplayMessage(listOfMessages = listOfMessages)
}

@Composable
fun DisplayMessage(listOfMessages: List<Sms?>) {

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom,
        reverseLayout = true
    ) {
        items(listOfMessages) { message ->
            MessageContent(
                sms = message
            )
        }
    }
}

@Composable
fun MessageContent(sms: Sms?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, end = 10.dp, start = 10.dp, bottom = 5.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {

            Text(
                text = "From: ${sms?.sender.toString()}",
                color = MaterialTheme.colors.TextColor,
                fontSize = MaterialTheme.typography.subtitle1.fontSize,
                fontWeight = FontWeight.Bold
            )
            if (sms != null) {
                Text(
                    text = convertTimeStampToDate(sms.createdAt.toLong()),
                    color = MediumGray,
                    fontSize = MaterialTheme.typography.subtitle2.fontSize,
                    modifier = Modifier.padding(bottom = 2.dp),
                    maxLines = 1
                )
                Text(
                    text = sms.content,
                    color = MaterialTheme.colors.TextColor,
                    fontSize = MaterialTheme.typography.body1.fontSize
                )
                Text(
                    text = "Code: ${sms.code}",
                    color = MaterialTheme.colors.TextColor,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    fontWeight = FontWeight.Medium
                )
            }


        }
    }
}