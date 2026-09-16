package com.example.managementproduct.ui.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.managementproduct.ManagementProductApplication
import com.example.managementproduct.data.repository.KiosRepository
import com.example.managementproduct.model.ProfitReport
import com.example.managementproduct.model.ReportRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LaporanViewModel(
    private val repository: KiosRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(ReportRange.HARI_INI)
    val selectedRange = _selectedRange.asStateFlow()

    private val _profitReport = MutableStateFlow<ProfitReport?>(null)
    val profitReport = _profitReport.asStateFlow()

    init {
        loadReport()
    }

    fun setReportRange(range: ReportRange) {
        if (_selectedRange.value != range) {
            _selectedRange.value = range
            loadReport()
        }
    }

    fun refreshReport() {
        loadReport()
    }

    private var reportJob: kotlinx.coroutines.Job? = null

    private fun loadReport() {
        reportJob?.cancel()
        reportJob = viewModelScope.launch {
            repository.getProfitReport(_selectedRange.value).collect { report ->
                _profitReport.value = report
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ManagementProductApplication)
                LaporanViewModel(application.container.kiosRepository)
            }
        }
    }
}
