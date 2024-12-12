package com.rsmax.usbserialtrack

import android.R
import android.content.Context
import android.hardware.usb.UsbManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.HexDump
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.launch


class TerminalViewModel : ViewModel(), SerialInputOutputManager.Listener{

    private val WRITE_WAIT_MILLIS = 1000
    private var usbSerialPort: UsbSerialPort? = null
    private var usbIoManager: SerialInputOutputManager? = null
    private val _receivedData = mutableStateOf("")
    var connected : Boolean = false
    val receivedData: State<String> = _receivedData

    fun connect(context: Context, deviceId: Int, portNum: Int, baudRate: Int) {
        viewModelScope.launch {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val driver = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager).firstOrNull { it.device.deviceId == deviceId }
            if (driver != null) {
                val connection = usbManager.openDevice(driver.device)
                if (connection != null) {
                    usbSerialPort = driver.ports[portNum]
                    usbSerialPort?.open(connection)
                    usbSerialPort?.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                    usbIoManager = SerialInputOutputManager(usbSerialPort, this@TerminalViewModel)
                    usbIoManager?.start()
                    connected = true
                    Toast.makeText(context, "device connected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to open USB connection", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "USB device not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewData(data: ByteArray) {
        _receivedData.value += String(data)
    }

    override fun onRunError(e: Exception) {
        // Handle error
    }

    override fun onCleared() {
        super.onCleared()
        usbIoManager?.stop()
        usbSerialPort?.close()
    }

    fun send(context: Context,str: String) {

        if (!connected) {
            Toast.makeText(context, "not connected", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val data = (str + '\n').toByteArray()
            usbSerialPort!!.write(data, WRITE_WAIT_MILLIS)
        } catch (e: java.lang.Exception) {
            onRunError(e)
        }
    }
}

@Composable
fun TerminalScreen(deviceId: Int, portNum: Int, baudRate: Int) {
    val context = LocalContext.current
    val viewModel: TerminalViewModel = viewModel()
    val receivedData by viewModel.receivedData

    LaunchedEffect(Unit) {
        viewModel.connect(context, deviceId, portNum, baudRate)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = "Terminal Screen")
        Text(text = "Received Data:")
        Text(text = receivedData)
        Button(onClick = {
            // Handle button click
            viewModel.send(context,"mensaje de prueba")
        }) {
            Text(text = "Send Data")
        }
    }
}