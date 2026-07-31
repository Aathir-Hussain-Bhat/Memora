package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Note
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    viewModel: MemoraViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Knowledge Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offset += pan
                    }
                }
        ) {
            val textMeasurer = rememberTextMeasurer()
            val primaryColor = MaterialTheme.colorScheme.primary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            
            // Randomly position nodes for the graph (with fixed seed so it doesn't jump around on recompose)
            val random = remember(notes.size) { Random(42) }
            val nodePositions = remember(notes) {
                notes.associateWith { 
                    Offset(
                        x = random.nextFloat() * 2000f - 1000f,
                        y = random.nextFloat() * 2000f - 1000f
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                val centerOffset = Offset(size.width / 2, size.height / 2)
                
                // Draw connections based on category match
                val nodesList = nodePositions.toList()
                for (i in nodesList.indices) {
                    for (j in i + 1 until nodesList.size) {
                        val (note1, pos1) = nodesList[i]
                        val (note2, pos2) = nodesList[j]
                        
                        if (note1.category == note2.category) {
                            drawLine(
                                color = primaryColor.copy(alpha = 0.3f),
                                start = centerOffset + pos1,
                                end = centerOffset + pos2,
                                strokeWidth = 2f
                            )
                        }
                    }
                }

                // Draw nodes
                nodePositions.forEach { (note, pos) ->
                    val nodeCenter = centerOffset + pos
                    drawCircle(
                        color = primaryColor,
                        radius = 20f,
                        center = nodeCenter
                    )
                    
                    val title = if (note.title.isNotBlank()) note.title else "Voice Note"
                    val textLayoutResult = textMeasurer.measure(
                        text = title,
                        style = TextStyle(color = onSurfaceColor)
                    )
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = nodeCenter.x - textLayoutResult.size.width / 2,
                            y = nodeCenter.y + 25f
                        )
                    )
                }
            }
        }
    }
}
