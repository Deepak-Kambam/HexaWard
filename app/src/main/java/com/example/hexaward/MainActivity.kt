package com.example.hexaward

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hexaward.presentation.DashboardScreen
import com.example.hexaward.presentation.MainViewModel
import com.example.hexaward.ui.theme.HexaWardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HexaWardTheme {
                val viewModel: MainViewModel = hiltViewModel()
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
