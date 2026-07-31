package com.example.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query)
    }

    suspend fun insert(note: Note) = noteDao.insertNote(note)

    suspend fun deleteById(id: Int) = noteDao.deleteNoteById(id)
}
