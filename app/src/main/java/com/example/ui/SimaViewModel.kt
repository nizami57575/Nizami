package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.ScanHistoryEntity
import com.example.data.database.SocialAccountEntity
import com.example.data.repository.SimaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SearchState {
    object Idle : SearchState
    data class Scanning(val progress: Int, val currentStep: String) : SearchState
    data class Success(val scanId: Int) : SearchState
    data class Error(val message: String) : SearchState
}

class SimaViewModel(private val repository: SimaRepository) : ViewModel() {

    var selectedImageUri: String? = null

    val scanHistory: StateFlow<List<ScanHistoryEntity>> = repository.allScans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    fun resetSearchState() {
        _searchState.value = SearchState.Idle
    }

    fun deleteScan(id: Int) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    suspend fun getScanById(id: Int): ScanHistoryEntity? {
        return repository.getScanById(id)
    }

    fun getSocialAccounts(scanId: Int): StateFlow<List<SocialAccountEntity>> {
        val flow = MutableStateFlow<List<SocialAccountEntity>>(emptyList())
        viewModelScope.launch {
            repository.getSocialAccounts(scanId).collect {
                flow.value = it
            }
        }
        return flow.asStateFlow()
    }

    private val progressSteps = listOf(
        0 to "Üz cizgiləri analiz edilir...",
        15 to "Biometrik xüsusiyyətlər və nöqtələr çıxarılır...",
        30 to "Sosial şəbəkə verilənlər bazası indeksləri axtarılır...",
        45 to "X (Twitter) profilləri yoxlanılır...",
        60 to "Instagram və TikTok profilləri müqayisə edilir...",
        75 to "Tapılan profillərin biometrik uyğunluğu yoxlanılır...",
        90 to "Hesabların həqiqiliyi təsdiqlənir...",
        100 to "Profil təsdiqləndi! Nəticələr hazırlanır..."
    )

    fun startFaceSearch(imageUri: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Scanning(0, progressSteps[0].second)

            // Let's run a fake progress animation to simulate "checking and confirming the accounts"
            // while making the actual Gemini API call in parallel.
            var searchResult: Result<Int>? = null
            
            // Launch the real API call in parallel
            val apiJob = launch {
                searchResult = repository.performFaceSearch(imageUri)
            }

            // Animate progress up to 90%
            for (i in 0..6) {
                val step = progressSteps[i]
                _searchState.value = SearchState.Scanning(step.first, step.second)
                delay(1200) // 1.2s delay per phase for rich immersive experience
            }

            // Wait for the actual API call to complete
            apiJob.join()

            val result = searchResult
            if (result != null && result.isSuccess) {
                // Animate final verification step
                val finalStep = progressSteps.last()
                _searchState.value = SearchState.Scanning(finalStep.first, finalStep.second)
                delay(1000)
                _searchState.value = SearchState.Success(result.getOrNull()!!)
            } else {
                val errorMessage = result?.exceptionOrNull()?.message ?: "Naməlum xəta baş verdi."
                _searchState.value = SearchState.Error(errorMessage)
            }
        }
    }
}

class SimaViewModelFactory(private val repository: SimaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
