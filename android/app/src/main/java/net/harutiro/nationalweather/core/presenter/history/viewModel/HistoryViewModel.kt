package net.harutiro.nationalweather.core.presenter.history.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import net.harutiro.nationalweather.features.stayLogDB.entities.StayLogEntity
import net.harutiro.nationalweather.features.stayLogDB.repositories.StayLogRepository
import net.harutiro.nationalweather.features.stayLogDB.repositories.StayLogRepositoryImpl

class HistoryViewModel : ViewModel() {
    private val stayRepo: StayLogRepository = StayLogRepositoryImpl()

    val logs: StateFlow<List<StayLogEntity>> = stayRepo.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList(),
    )
}
