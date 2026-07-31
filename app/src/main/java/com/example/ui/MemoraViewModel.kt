package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.BuildConfig
import com.example.data.Content
import com.example.data.GenerateContentRequest
import com.example.data.Part
import com.example.data.RetrofitClient

data class ChatMessage(val text: String, val isUser: Boolean)

class MemoraViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! Ask me anything about your memories.", false))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allNotes
            } else {
                repository.searchNotes(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            var category = "General"
            
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val prompt = "Categorize the following note into one single short category name (e.g., Work, Personal, Ideas, Travel, Health). Note Title: $title. Note Content: $content. Respond ONLY with the category name."
                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt))))
                    )
                    val response = RetrofitClient.service.generateContent(apiKey, request)
                    val aiCategory = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                    if (!aiCategory.isNullOrBlank()) {
                        category = aiCategory.take(20) // Limit length
                    }
                } catch (e: Exception) {
                    // Fallback to General
                }
            }
            
            repository.insert(Note(title = title, content = content, category = category))
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun askAiQuestion(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _chatMessages.value = _chatMessages.value + ChatMessage(question, true)
            _isAiLoading.value = true

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                _chatMessages.value = _chatMessages.value + ChatMessage("Please configure the Gemini API Key to use this feature.", false)
                _isAiLoading.value = false
                return@launch
            }

            try {
                val allNotesText = notes.value.take(20).joinToString("\n") { 
                    "Title: ${it.title}\nContent: ${it.content}\nCategory: ${it.category}\n---" 
                }

                val prompt = "You are Memora AI, an assistant that answers questions based on the user's notes.\n\nUser Notes:\n$allNotesText\n\nUser Question: $question\n\nAnswer the question concisely using the provided notes."
                
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )
                
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I'm sorry, I couldn't generate an answer."
                
                _chatMessages.value = _chatMessages.value + ChatMessage(answer.trim(), false)
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage("Error connecting to AI: ${e.message}", false)
            } finally {
                _isAiLoading.value = false
            }
        }
    }
}
