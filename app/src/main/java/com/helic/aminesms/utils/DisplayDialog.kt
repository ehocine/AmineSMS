package com.helic.aminesms.utils

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.helic.aminesms.R
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.ButtonTextColor
import com.helic.aminesms.presentation.ui.theme.DialogNoText
import com.helic.aminesms.presentation.ui.theme.backgroundColor


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
            backgroundColor = MaterialTheme.colors.backgroundColor,
            text = message,
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor),
                    onClick = {
                        onYesClicked()
                        closeDialog()
                    })
                {
                    Text(
                        text = stringResource(R.string.yes),
                        color = MaterialTheme.colors.ButtonTextColor
                    )
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


@Composable
fun DisplayInfoDialog(
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
            backgroundColor = MaterialTheme.colors.backgroundColor,
            text = message,
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor),
                    onClick = {
                        onYesClicked()
                        closeDialog()
                    })
                {
                    Text(
                        text = stringResource(R.string.ok),
                        color = MaterialTheme.colors.ButtonTextColor
                    )
                }
            },
            onDismissRequest = { closeDialog() }
        )
    }
}
