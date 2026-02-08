package com.example.mobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mobileapp.ui.nav.AppNav
import com.example.mobileapp.ui.theme.MobileAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobileAppTheme {
                AppNav()
            }
        }
    }
}
