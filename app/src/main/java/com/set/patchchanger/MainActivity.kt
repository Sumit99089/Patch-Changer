package com.set.patchchanger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.set.patchchanger.presentation.screens.MainScreen
import com.set.patchchanger.presentation.viewmodel.MainViewModel
import com.set.patchchanger.ui.theme.PatchChangerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isSystemBarsVisible = true // State to track visibility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This enables drawing behind the system bars
        enableEdgeToEdge()

        setContent {
            PatchChangerTheme {
                MainScreen(viewModel = viewModel, onToggleFullscreen = {
                    // Toggle the visibility based on the current state
                    isSystemBarsVisible = !isSystemBarsVisible
                    toggleSystemBars(isSystemBarsVisible)
                })
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Re-apply the fullscreen state when the window gains focus,
        // for example, when returning to the app.
        if (hasFocus) {
            toggleSystemBars(isSystemBarsVisible)
        }
    }

    private fun toggleSystemBars(show: Boolean) {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        // Configure the behavior for showing transient bars by swipe
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (show) {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        } else {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanup()
    }
}