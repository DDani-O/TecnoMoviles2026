package com.fintrack.mobile.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PrimarySecondaryActions(
    @StringRes primaryLabelRes: Int,
    @StringRes secondaryLabelRes: Int,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Button(onClick = onPrimary, modifier = buttonModifier) {
            Text(text = stringResource(primaryLabelRes))
        }
        OutlinedButton(onClick = onSecondary, modifier = buttonModifier) {
            Text(text = stringResource(secondaryLabelRes))
        }
    }
}
