package com.set.patchchanger

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.set.patchchanger.presentation.screens.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Tell the system to draw your app behind system bars PERMANENTLY.
        // This stops the resizing/glitching when the bar appears.
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            // BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE makes them an OVERLAY
            // so they don't push your content down.
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            MainScreen()
        }
    }


//    private fun hideSystemBars() {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        val controller = WindowCompat.getInsetsController(window, window.decorView)
//        controller.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        controller.hide(WindowInsetsCompat.Type.systemBars())
//    }
//
//    // Ensure bars stay hidden if window focus changes
//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            hideSystemBars()
//        }
//    }
}