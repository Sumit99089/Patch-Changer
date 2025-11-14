package com.set.patchchanger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.set.patchchanger.presentation.screens.MainScreen
import com.set.patchchanger.presentation.viewmodel.MainViewModel
import com.set.patchchanger.ui.theme.PatchChangerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PatchChangerTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanup()
    }
}

@PreviewLightDark
@Composable
fun Preview() {
    PatchChangerTheme {
        // Preview won't have a real ViewModel, so functionality will be limited
        MainScreen()
    }
}