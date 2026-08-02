package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import com.example.data.SettingsRepository
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
import com.example.data.OpenRouterClient
import com.example.data.OpenRouterMessage
import com.example.data.OpenRouterRequest

data class ChatMessage(val text: String, val isUser: Boolean)

class MemoraViewModel(
    private val repository: NoteRepository,
    val settings: SettingsRepository
) : ViewModel() {

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

    fun testConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val apiKey = settings.openRouterApiKey
            if (apiKey.isBlank()) {
                onResult(false, "API Key is empty")
                return@launch
            }
            if (settings.mockMode) {
                onResult(true, "Mock mode is ON - simulated success")
                return@launch
            }
            try {
                val request = OpenRouterRequest(
                    model = settings.modelName,
                    messages = listOf(OpenRouterMessage("user", "Hello"))
                )
                val response = OpenRouterClient.service.chatCompletions("Bearer $apiKey", request)
                if (response.isSuccessful) {
                    onResult(true, "Connection successful")
                } else {
                    onResult(false, "Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                onResult(false, "Network error: Cannot reach OpenRouter. ${e.message}")
            }
        }
    }

    fun askAiQuestion(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _chatMessages.value = _chatMessages.value + ChatMessage(question, true)
            _isAiLoading.value = true

            if (settings.mockMode) {
                kotlinx.coroutines.delay(1000)
                _chatMessages.value = _chatMessages.value + ChatMessage("Mock response: I am in mock mode. Here is a simulated response to your question.", false)
                _isAiLoading.value = false
                return@launch
            }

            val apiKey = settings.openRouterApiKey
            if (apiKey.isBlank()) {
                _chatMessages.value = _chatMessages.value + ChatMessage("Invalid API Key — re-enter in Settings. OpenRouter API key is required.", false)
                _isAiLoading.value = false
                return@launch
            }

            try {
                val allNotesText = notes.value.take(20).joinToString("\n") { 
                    "Title: ${it.title}\nContent: ${it.content}\nCategory: ${it.category}\n---" 
                }

                val messages = mutableListOf(
                    OpenRouterMessage("system", "You are Sparky, an offline-minded assistant. User Notes:\n$allNotesText")
                )
                
                // Add recent history to maintain context
                _chatMessages.value.takeLast(10).forEach {
                    messages.add(OpenRouterMessage(if(it.isUser) "user" else "assistant", it.text))
                }

                val request = OpenRouterRequest(
                    model = settings.modelName,
                    messages = messages
                )
                
                val response = OpenRouterClient.service.chatCompletions("Bearer $apiKey", request)
                if (response.isSuccessful) {
                    val answer = response.body()?.choices?.firstOrNull()?.message?.content ?: "Sparky couldn’t generate an answer — try rephrasing."
                    _chatMessages.value = _chatMessages.value + ChatMessage(answer.trim(), false)
                } else {
                    val errorMsg = when(response.code()) {
                        401, 403 -> "Invalid key or CORS blocked. For production, use a secure proxy/server-side relay. For local testing, toggle Mock Mode."
                        429 -> "Rate limit reached — try again in a few seconds."
                        in 500..599 -> "Server error — try again later."
                        else -> "Sparky couldn’t generate an answer — try rephrasing."
                    }
                    _chatMessages.value = _chatMessages.value + ChatMessage(errorMsg, false)
                }
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage("Network error: Cannot reach OpenRouter. ${e.message}", false)
            } finally {
                _isAiLoading.value = false
            }
        }
    }
}

