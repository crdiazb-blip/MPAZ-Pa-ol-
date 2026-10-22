package com.example.mpazpaolr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mpazpaolr.ui.navigation.AppNavigation
import com.example.mpazpaolr.ui.theme.MPAZPañolrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MPAZPañolrTheme {
                AppNavigation()
            }
        }
    }
}
