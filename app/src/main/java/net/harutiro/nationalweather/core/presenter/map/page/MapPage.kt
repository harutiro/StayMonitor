package net.harutiro.nationalweather.core.presenter.map.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import net.harutiro.nationalweather.R
import net.harutiro.nationalweather.core.presenter.map.component.PinEditBottomSheet
import net.harutiro.nationalweather.core.presenter.map.viewModel.MapViewModel
import net.harutiro.nationalweather.core.presenter.permission.PermissionRequestBanner
import net.harutiro.nationalweather.core.presenter.permission.rememberLocationPermissionState
import net.harutiro.nationalweather.features.map.MapSetupController
import net.harutiro.nationalweather.features.map.MapTapController
import net.harutiro.nationalweather.features.map.OsmMapView
import net.harutiro.nationalweather.features.map.clearPins
import net.harutiro.nationalweather.features.map.renderPins
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapPage(viewModel: MapViewModel = viewModel()) {
    val pins by viewModel.pins.collectAsStateWithLifecycle()
    val pending by viewModel.pendingLatLng.collectAsStateWithLifecycle()
    val selectedPin by viewModel.selectedPin.collectAsStateWithLifecycle()
    val permissionState = rememberLocationPermissionState()

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }

    // 初回起動時にフォアグラウンドの位置情報と通知をまとめて要求
    LaunchedEffect(Unit) {
        if (!permissionState.hasForegroundLocation || !permissionState.hasNotifications) {
            permissionState.requestForegroundAndNotifications()
        }
    }
    // フォアグラウンドが取れた後にバックグラウンドを要求 (Android 10+ で必須)
    LaunchedEffect(permissionState.hasForegroundLocation) {
        if (permissionState.needsBackgroundRequest) {
            permissionState.requestBackground()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            onLoad = { view ->
                mapView = view
                MapSetupController(view.context, view).setupMapWithLocation()
                MapTapController(view) { point ->
                    viewModel.setPendingPin(point.latitude, point.longitude)
                }
            },
        )

        LaunchedEffect(mapView, pins) {
            val view = mapView ?: return@LaunchedEffect
            view.clearPins()
            view.renderPins(
                pins = pins,
                onPinClick = { pinId -> viewModel.selectPin(pinId) },
                onPinDragEnd = { pinId, lat, lon -> viewModel.movePin(pinId, lat, lon) },
            )
        }

        LaunchedEffect(mapView, pending) {
            val view = mapView ?: return@LaunchedEffect
            // 既存の一時マーカーを削除
            view.overlays.removeAll { it is Marker && it.id == PENDING_MARKER_ID }
            pending?.let { (lat, lon) ->
                val marker = Marker(view).apply {
                    id = PENDING_MARKER_ID
                    position = GeoPoint(lat, lon)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "登録予定"
                }
                view.overlays.add(marker)
            }
            view.invalidate()
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    if (pending != null) {
                        inputName = ""
                        showDialog = true
                    }
                },
                enabled = pending != null && permissionState.hasForegroundLocation,
            ) {
                Text(text = stringResource(id = R.string.register))
            }
            Button(onClick = { viewModel.stopAll() }) {
                Text(text = "全部停止")
            }
            Button(
                onClick = {
                    val view = mapView ?: return@Button
                    MapSetupController(view.context, view).setupMapWithLocation()
                },
            ) {
                Text(text = "現在地に戻る")
            }
        }

        PermissionRequestBanner(
            state = permissionState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
        )

        selectedPin?.let { pin ->
            PinEditBottomSheet(
                pin = pin,
                onRename = { newName -> viewModel.renamePin(pin.id, newName) },
                onDelete = { viewModel.removePin(pin.id) },
                onDismiss = { viewModel.selectPin(null) },
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                viewModel.clearPending()
            },
            title = { Text(text = stringResource(id = R.string.register_pin_title)) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text(text = stringResource(id = R.string.pin_name_hint)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            viewModel.registerPin(inputName.trim())
                            showDialog = false
                        }
                    },
                ) {
                    Text(text = stringResource(id = R.string.register))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        viewModel.clearPending()
                    },
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        )
    }
}

private const val PENDING_MARKER_ID = "pending_marker"
