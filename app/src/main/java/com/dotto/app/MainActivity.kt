package com.dotto.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dotto.app.navigation.DottoNavGraph
import com.dotto.app.ui.theme.DottoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DottoTheme {
                DottoNavGraph(app = application as DottoApp)
            }
        }
    }
}
