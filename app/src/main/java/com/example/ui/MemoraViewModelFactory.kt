package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.NoteRepository
import com.example.data.SettingsRepository

class MemoraViewModelFactory(
    private val repository: NoteRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoraViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
