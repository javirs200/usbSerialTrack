package com.rsmax.usbserialtrack

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber

data class ListItem(val device: UsbDevice?, val port: Int, val driver: UsbSerialDriver?)

@Composable
fun DevicesList(navController: NavController) {
    val context = LocalContext.current
    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    val usbDefaultProber = UsbSerialProber.getDefaultProber()
    val listItems = remember { mutableStateListOf<ListItem>() }

    LaunchedEffect(Unit) {
        listItems.clear()
        listItems.add(ListItem(device = null,port = 1 ,driver = null))
        for (device in usbManager.deviceList.values) {
            val driver = usbDefaultProber.probeDevice(device)
            if (driver != null) {
                for (port in driver.ports.indices) {
                    listItems.add(ListItem(device, port, driver))
                }
            } else {
                listItems.add(ListItem(device, 0, null))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        LazyColumn {
            items(listItems) { item ->
                DeviceListItem(item,navController)
            }
        }
    }
}

@Composable
fun DeviceListItem(item: ListItem, navController: NavController) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (item.driver == null) {
                    Toast
                        .makeText(context, "no driver , navigate in dev mode", Toast.LENGTH_SHORT)
                        .show()
                    val route = "crono/${-1}/${item.port}/${0}"
                    navController.navigate(route)
                } else {
                    val route = "crono/${item.device?.deviceId}/${item.port}/${115200}"
                    navController.navigate(route)
                }
            },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Device: ${item.device?.deviceName}",
            )
            Text(
                text = "Port: ${item.port}",
            )
        }
    }
}