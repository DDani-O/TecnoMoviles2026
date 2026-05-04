package com.fintrack.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fintrack.mobile.R

@Composable
fun AdjustTicketScreen(onDone: () -> Unit) {
    var discount by rememberSaveable { mutableFloatStateOf(0f) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.adjust_ticket_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(text = stringResource(R.string.adjust_ticket_discount, (discount * 100).toInt()))
            Slider(value = discount, onValueChange = { discount = it })
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.adjust_ticket_apply))
            }
        }
    }
}
