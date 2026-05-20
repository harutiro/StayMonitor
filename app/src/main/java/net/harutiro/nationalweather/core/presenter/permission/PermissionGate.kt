package net.harutiro.nationalweather.core.presenter.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Stable
class LocationPermissionState internal constructor(
    val hasForegroundLocation: Boolean,
    val hasBackgroundLocation: Boolean,
    val hasNotifications: Boolean,
    val requestForegroundAndNotifications: () -> Unit,
    val requestBackground: () -> Unit,
    val openAppSettings: () -> Unit,
) {
    val allGranted: Boolean
        get() = hasForegroundLocation && hasBackgroundLocation && hasNotifications

    val needsBackgroundRequest: Boolean
        get() = hasForegroundLocation && !hasBackgroundLocation
}

@Composable
fun rememberLocationPermissionState(): LocationPermissionState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasForeground by remember { mutableStateOf(checkForegroundLocation(context)) }
    var hasBackground by remember { mutableStateOf(checkBackgroundLocation(context)) }
    var hasNotifications by remember { mutableStateOf(checkPostNotifications(context)) }

    // 設定画面から戻ってきたタイミングなどで再評価する
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasForeground = checkForegroundLocation(context)
                hasBackground = checkBackgroundLocation(context)
                hasNotifications = checkPostNotifications(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val foregroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        hasForeground = checkForegroundLocation(context)
        hasNotifications = checkPostNotifications(context)
    }

    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        hasBackground = checkBackgroundLocation(context)
    }

    return remember(hasForeground, hasBackground, hasNotifications) {
        LocationPermissionState(
            hasForegroundLocation = hasForeground,
            hasBackgroundLocation = hasBackground,
            hasNotifications = hasNotifications,
            requestForegroundAndNotifications = {
                val perms = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                foregroundLauncher.launch(perms.toTypedArray())
            },
            requestBackground = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            },
            openAppSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
        )
    }
}

@Composable
fun PermissionRequestBanner(
    state: LocationPermissionState,
    modifier: Modifier = Modifier,
) {
    if (state.allGranted) return
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "アプリの動作には以下の権限が必要です",
                style = MaterialTheme.typography.titleMedium,
            )
            if (!state.hasForegroundLocation) {
                Text(text = "・位置情報 (高精度)")
            }
            if (state.needsBackgroundRequest) {
                Text(text = "・バックグラウンドでの位置情報")
            }
            if (!state.hasNotifications) {
                Text(text = "・通知の表示")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    !state.hasForegroundLocation || !state.hasNotifications -> {
                        Button(onClick = state.requestForegroundAndNotifications) {
                            Text(text = "許可する")
                        }
                    }
                    state.needsBackgroundRequest -> {
                        Button(onClick = state.requestBackground) {
                            Text(text = "バックグラウンドも許可")
                        }
                    }
                }
                OutlinedButton(onClick = state.openAppSettings) {
                    Text(text = "設定を開く")
                }
            }
        }
    }
}

private fun checkForegroundLocation(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

private fun checkBackgroundLocation(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

private fun checkPostNotifications(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
