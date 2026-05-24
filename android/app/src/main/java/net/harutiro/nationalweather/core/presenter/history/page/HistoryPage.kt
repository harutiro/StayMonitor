package net.harutiro.nationalweather.core.presenter.history.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import net.harutiro.nationalweather.core.presenter.history.viewModel.HistoryViewModel
import net.harutiro.nationalweather.features.stayLogDB.entities.StayLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryPage(viewModel: HistoryViewModel = viewModel()) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "合計 ${logs.size} 件")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(logs, key = { it.id }) { log ->
                StayLogRow(log)
            }
        }
    }
}

@Composable
private fun StayLogRow(log: StayLogEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = log.pinName)
            Text(text = "入室: ${formatDateTime(log.enteredAt)}")
            Text(text = "退出: ${log.exitedAt?.let { formatDateTime(it) } ?: "滞在中"}")
            Text(text = "滞在時間: ${formatDuration(log.enteredAt, log.exitedAt)}")
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
    return format.format(Date(timestamp))
}

private fun formatDuration(enteredAt: Long, exitedAt: Long?): String {
    val end = exitedAt ?: System.currentTimeMillis()
    val minutes = ((end - enteredAt) / 60_000L).coerceAtLeast(0)
    return "${minutes}分"
}
