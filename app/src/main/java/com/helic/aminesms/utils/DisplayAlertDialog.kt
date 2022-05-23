package com.helic.aminesms.utils

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.helic.aminesms.R
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.DialogNoText


@Composable
fun DisplayAlertDialog(
    title: String,
    message: @Composable (() -> Unit),
    openDialog: Boolean,
    closeDialog: () -> Unit,
    onYesClicked: () -> Unit,
) {
    if (openDialog) {
        AlertDialog(
            title = {
                Text(
                    text = title,
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    fontWeight = FontWeight.Bold
                )
            },
            text = message,
//            {
//                Text(
//                    text = message,
//                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
//                    fontWeight = FontWeight.Normal
//                )
//            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor),
                    onClick = {
                        onYesClicked()
                        closeDialog()
                    })
                {
                    Text(text = stringResource(R.string.yes), color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { closeDialog() })
                {
                    Text(
                        text = stringResource(R.string.no),
                        color = MaterialTheme.colors.DialogNoText
                    )
                }
            },
            onDismissRequest = { closeDialog() }
        )
    }
}
