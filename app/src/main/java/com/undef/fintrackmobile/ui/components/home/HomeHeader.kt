package com.undef.fintrackmobile.ui.components.home
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * HomeHeader: Fila superior de la pantalla de inicio que incluye el perfil del usuario
 * y el acceso a notificaciones.
 */
@Composable
fun HomeHeader(
    displayName: String,
    profileImageUri: String?,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    notificationCount: Int = 1,
) {
    val colors = FintrackTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onProfileClick() }
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(displayName = displayName, profileImageUri = profileImageUri)
            Spacer(modifier = Modifier.size(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.home_greeting, displayName),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.home_welcome),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        
        BadgedBox(
            badge = {
                if (notificationCount > 0) {
                    Badge(
                        modifier = Modifier.padding(4.dp),
                        containerColor = colors.pastelRed,
                    ) {
                        Text(text = notificationCount.toString(), color = colors.neutralWhite)
                    }
                }
            }
        ) {
            Surface(
                shape = CircleShape,
                color = colors.celestePale,
                modifier = Modifier.size(48.dp)
            ) {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.home_notifications),
                        tint = colors.celesteDeep
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    displayName: String,
    profileImageUri: String?,
) {
    val colors = FintrackTheme.colors
    Surface(
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        color = colors.celestePale,
    ) {
        if (!profileImageUri.isNullOrEmpty()) {
            AsyncImage(
                model = profileImageUri,
                contentDescription = stringResource(R.string.home_profile_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (displayName.isBlank()) {
                    Icon(
                        imageVector = Icons.Filled.Pets,
                        contentDescription = null,
                        tint = colors.celesteDeep,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = initialsFor(displayName),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.celesteDeep,
                    )
                }
            }
        }
    }
}

private fun initialsFor(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).asSequence().filter { it.isNotBlank() }.take(2).toList()
    return if (parts.isEmpty()) "U" else parts.joinToString("") { it.first().uppercase() }
}
