package com.fintrack.mobile.ui.screens

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.components.TitleSubtitleCard
import com.fintrack.mobile.ui.viewmodel.ExploreUiState
import com.fintrack.mobile.ui.viewmodel.ExploreViewModel

private const val DEFAULT_BASE_CODE = "USD"
private const val DEFAULT_TARGET_CODE = "ARS"

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    val baseLabel = stringResource(R.string.currency_usd)
    val targetLabel = stringResource(R.string.currency_ars)

    LaunchedEffect(DEFAULT_BASE_CODE, DEFAULT_TARGET_CODE) {
        viewModel.loadRate(base = DEFAULT_BASE_CODE, target = DEFAULT_TARGET_CODE)
    }

    val promos = remember {
        listOf(
            PromoItem(R.string.promo_title_1, R.string.promo_subtitle_1),
            PromoItem(R.string.promo_title_2, R.string.promo_subtitle_2),
            PromoItem(R.string.promo_title_3, R.string.promo_subtitle_3)
        )
    }

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.explore_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.explore_search_placeholder)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.explore_exchange_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        when (val current = state) {
                            ExploreUiState.Loading -> CircularProgressIndicator()
                            is ExploreUiState.Ready -> {
                                Text(
                                    text = stringResource(
                                        R.string.explore_exchange_label,
                                        baseLabel,
                                        current.rate,
                                        targetLabel
                                    ),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (current.isFallback) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stringResource(R.string.explore_network_fallback),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.explore_promos_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(promos, key = { it.titleRes }) { promo ->
                TitleSubtitleCard(
                    title = stringResource(promo.titleRes),
                    subtitle = stringResource(promo.subtitleRes)
                )
            }

            item {
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://www.carrefour.com.ar".toUri()
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.explore_open_promos))
                }
            }
        }
    }
}

private data class PromoItem(
    @get:StringRes val titleRes: Int,
    @get:StringRes val subtitleRes: Int,
)
