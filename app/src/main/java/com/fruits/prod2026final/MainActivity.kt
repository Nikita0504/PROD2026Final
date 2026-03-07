package com.fruits.prod2026final

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fruits.prod2026final.navigation.AppNavGraph
import com.fruits.prod2026final.ui.theme.PROD2026finalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PROD2026finalTheme {
                AppNavGraph()
            }
        }
    }
}
