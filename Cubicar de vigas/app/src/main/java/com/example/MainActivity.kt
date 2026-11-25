package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.BeamCubicadorApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BeamViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: BeamViewModel by viewModels {
    BeamViewModel.provideFactory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        BeamCubicadorApp(viewModel = viewModel)
      }
    }
  }
}

