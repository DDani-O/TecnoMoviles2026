package com.fintrack.mobile.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fintrack.mobile.R

@Composable
fun AuthForm(
    @StringRes titleRes: Int,
    @StringRes primaryLabelRes: Int,
    @StringRes secondaryLabelRes: Int,
    onPrimary: (String, String) -> Unit,
    onSecondary: () -> Unit
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(titleRes),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            LabeledTextField(
                value = displayName,
                onValueChange = { displayName = it },
                labelRes = R.string.label_name,
                placeholderRes = R.string.placeholder_name,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            LabeledTextField(
                value = email,
                onValueChange = { email = it },
                labelRes = R.string.label_email,
                placeholderRes = R.string.placeholder_email,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrimarySecondaryActions(
                primaryLabelRes = primaryLabelRes,
                secondaryLabelRes = secondaryLabelRes,
                onPrimary = { onPrimary(displayName, email) },
                onSecondary = onSecondary
            )
        }
    }
}
