package com.set.patchchanger

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.app.ComponentActivity
import androidx.core.view.WindowCompat.enableEdgeToEdge
import com.set.patchchanger.presentation.screens.MainScreen
import com.set.patchchanger.ui.theme.PatchChangerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PatchChangerTheme {
                MainScreen()
            }
        }
    }
}

@PreviewLightDark
@Composable
fun test() {
    PatchChangerTheme {
        MainScreen()
    }
}