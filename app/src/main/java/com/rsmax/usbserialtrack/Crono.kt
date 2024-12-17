package com.rsmax.usbserialtrack

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.usb.UsbManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.launch


class CronoViewModel : ViewModel(), SerialInputOutputManager.Listener{

    private val writeWaitMilliseconds = 1000
    private var usbSerialPort: UsbSerialPort? = null
    private var usbIoManager: SerialInputOutputManager? = null
    private val _receivedData = mutableStateOf("")
    private var connected : Boolean = false
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
                    usbIoManager = SerialInputOutputManager(usbSerialPort, this@CronoViewModel)
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
            usbSerialPort!!.write(data, writeWaitMilliseconds)
        } catch (e: java.lang.Exception) {
            onRunError(e)
        }
    }
}

@Composable
fun CronoScreen(deviceId: Int, portNum: Int, baudRate: Int) {
    val context = LocalContext.current
    val viewModel: CronoViewModel = viewModel()
    val receivedData by viewModel.receivedData

    LaunchedEffect(Unit) {
        viewModel.connect(context, deviceId, portNum, baudRate)
    }

    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        Row(modifier = Modifier.fillMaxSize()){
            Column(modifier = Modifier
                .padding(40.dp)
                .weight(0.5f)
                .align(Alignment.CenterVertically)
            )
            {
                Text(text = "Crono Screen")
                Button(onClick = {
                    // Handle button click
                    viewModel.send(context,"mensaje de prueba")
                }) {
                    Text(text = "Send Data")
                }
            }
            Column(modifier = Modifier
                .padding(20.dp)
                .background(Color.LightGray)
                .weight(0.3f)
                .fillMaxSize()
                .align(Alignment.CenterVertically)
                .verticalScroll(rememberScrollState()))
            {
                Text(text = "Crono Screen")
                Text(text = "Received Data:")
                Text(text = receivedData)
            }
        }

}

@Preview(
    device = "spec:parent=virtualPoco,orientation=landscape", apiLevel = 34
)
@Composable
fun CronoPreview(){
    CronoScreen(0,0,115200)
}

