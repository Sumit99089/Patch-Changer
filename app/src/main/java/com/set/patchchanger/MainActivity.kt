package com.set.patchchanger

import android.os.Bundle
import androidx.activity.ComponentActivity
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
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        hideSystemBars()

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

    private fun hideSystemBars() {
        // Ensure the decor view fits system windows is false (Edge-to-Edge)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Hide both status and navigation bars to ensure full screen
        // Using Type.systemBars() as per your previous working code
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
    // Ensure bars stay hidden if window focus changes
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
//    }
    }
}