package com.poco.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.poco.app.navigation.PocoNavGraph
import com.poco.app.ui.theme.PocoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocoTheme {
                PocoNavGraph(app = application as PocoApp)
            }
        }
    }
}
