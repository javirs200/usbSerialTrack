package com.rsmax.usbserialtrack

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rsmax.usbserialtrack.ui.theme.UsbSerialTrackTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UsbSerialTrackTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.action)) {
            // Handle USB device attached action
        }
    }
}

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DeviceSelectionScreen(navController:NavController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text("Crono app usb")
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                   Row{
                       Button(
                           modifier = Modifier.padding(14.dp,0.dp),
                           onClick = {
                           navController.navigate("timeViewer")
                       }) {
                           Text(text = "view times")
                       }
                       Text(
                           modifier = Modifier
                               .fillMaxWidth(),
                           textAlign = TextAlign.Center,
                           text = "made by javi",
                       )
                   }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DevicesList(navController)
            }
        }
    }


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "devices") {
        composable("devices") { DeviceSelectionScreen(navController) }
        composable("crono/{deviceId}/{portNum}/{baudRate}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId")?.toInt() ?: 0
            val portNum = backStackEntry.arguments?.getString("portNum")?.toInt() ?: 0
            val baudRate = backStackEntry.arguments?.getString("baudRate")?.toInt() ?: 115200
            CronoScreen(deviceId, portNum, baudRate)
        }
        composable("timeViewer") { TimesScreen() }
    }
}