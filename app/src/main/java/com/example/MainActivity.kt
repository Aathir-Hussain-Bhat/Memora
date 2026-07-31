package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.NoteRepository
import com.example.ui.MemoraApp
import com.example.ui.MemoraViewModel
import com.example.ui.MemoraViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "memora-database"
        ).build()
        val repository = NoteRepository(db.noteDao())
        val viewModelFactory = MemoraViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MemoraViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MemoraApp(viewModel = viewModel)
            }
        }
    }
}
