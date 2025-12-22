package com.example.hexaward

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.hexaward.data.worker.SecurityWorker
import com.example.hexaward.data.worker.UpdateCheckWorker
import com.example.hexaward.presentation.DashboardScreen
import com.example.hexaward.presentation.MainViewModel
import com.example.hexaward.presentation.OnboardingScreen
import com.example.hexaward.presentation.SettingsScreen
import com.example.hexaward.presentation.SplashScreen
import com.example.hexaward.presentation.UpdateDialog
import com.example.hexaward.ui.theme.HexaWardTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // TEST: Trigger an immediate background scan to verify Hilt injection
        try {
            val testWork = OneTimeWorkRequestBuilder<SecurityWorker>().build()
            WorkManager.getInstance(this).enqueue(testWork)
            Log.d("HexaWardApp", "Manual Test Worker enqueued.")
        } catch (e: Exception) {
            Log.e("HexaWardApp", "Failed to enqueue test worker", e)
        }
        
        // Schedule periodic update checks
        UpdateCheckWorker.enqueuePeriodicCheck(this)
        
        // Trigger immediate update check for testing
        UpdateCheckWorker.checkNow(this)
        
        val sharedPref = getSharedPreferences("hexa_pref", Context.MODE_PRIVATE)
        
        setContent {
            HexaWardTheme {
                var showSplash by remember { mutableStateOf(true) }
                var showOnboarding by remember { 
                    mutableStateOf(sharedPref.getBoolean("onboarding_finished", false).not()) 
                }

                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (showSplash) {
                        SplashScreen(onAnimationFinished = {
                            showSplash = false
                        })
                    } else if (showOnboarding) {
                        OnboardingScreen(onFinished = {
                            sharedPref.edit { putBoolean("onboarding_finished", true) }
                            showOnboarding = false
                        })
                    } else {
                        HexaWardPager()
                    }
                }
            }
        }
    }
}

@Composable
fun HexaWardPager() {
    val viewModel: MainViewModel = hiltViewModel()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    // Remove auto-showing update dialog from main screen
    // It will only show in Settings page now

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = true,
        beyondViewportPageCount = 1
    ) { page ->
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    
                    alpha = lerp(
                        start = 1f,
                        stop = 0f,
                        fraction = pageOffset.coerceIn(0f, 1f)
                    )
                    
                    val scale = lerp(
                        start = 1f,
                        stop = 0.9f,
                        fraction = pageOffset.coerceIn(0f, 1f)
                    )
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            when (page) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
                1 -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
            }
        }
    }
}
