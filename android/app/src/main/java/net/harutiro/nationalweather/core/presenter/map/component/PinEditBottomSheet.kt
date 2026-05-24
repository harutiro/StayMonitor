package net.harutiro.nationalweather.core.presenter.map.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEditBottomSheet(
    pin: GeofencePinEntity,
    onRename: (newName: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(pin.id) { mutableStateOf(pin.name) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "ピン名") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { onRename(name.trim()) },
                    enabled = name.trim().isNotEmpty(),
                ) {
                    Text(text = "保存")
                }
            }

            Text(
                text = "緯度: ${"%.5f".format(pin.latitude)}, 経度: ${"%.5f".format(pin.longitude)}",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = "半径: ${pin.radiusMeters.toInt()} m",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = "ピンを長押し&ドラッグで位置を移動できます",
                style = MaterialTheme.typography.bodySmall,
            )

            OutlinedButton(
                onClick = { showConfirmDelete = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red,
                ),
            ) {
                Text(text = "削除")
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text(text = "ピンを削除しますか?") },
            text = { Text(text = "滞在履歴は残ります") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDelete = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red,
                    ),
                ) {
                    Text(text = "削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text(text = "キャンセル")
                }
            },
        )
    }
}
