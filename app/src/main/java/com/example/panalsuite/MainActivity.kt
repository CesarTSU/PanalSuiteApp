package com.example.panalsuite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.panalsuite.ui.navigation.AppNavHost
import com.example.panalsuite.ui.theme.PanalSuiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PanalSuiteApp).container
        setContent {
            PanalSuiteTheme {
                AppNavHost(container)
            }
        }
    }
}
