package com.rsmax.usbserialtrack

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.usb.UsbManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.launch
import java.lang.String.format
import java.util.Locale


class CronoViewModel : ViewModel(), SerialInputOutputManager.Listener{

    private val writeWaitMilliseconds = 1000
    private var usbSerialPort: UsbSerialPort? = null
    private var usbIoManager: SerialInputOutputManager? = null
    private val _receivedData = mutableStateOf("")
    private val _timeData = mutableStateOf("")
    private val _threshold = mutableIntStateOf(50)
    private var connected : Boolean = false
    val receivedData: State<String> = _receivedData
    val timeData : State<String> = _timeData
    val threshold : State<Int> = _threshold

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
                Toast.makeText(context, "USB device not found , enter in dev mode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewData(data: ByteArray) {
        _receivedData.value += String(data)
        var temString = String(data)
        if(temString.contains("time:")){
            temString = temString.substringAfter("time:").trim()
            val millis = temString.toIntOrNull() ?: return
            val minutes = (millis / 60000) % 60
            val seconds = (millis / 1000) % 60
            val milliseconds = millis % 1000
            val formattedTime = format(Locale.ROOT,"%02d:%02d.%03d", minutes, seconds, milliseconds)
            _timeData.value = formattedTime
        }
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

    fun setThreshold(value: Int) {
        _threshold.intValue = value
    }
}

@Composable
fun CronoScreen(deviceId: Int, portNum: Int, baudRate: Int) {
    val context = LocalContext.current
    val viewModel: CronoViewModel = viewModel()
    val receivedData by viewModel.receivedData
    val timeData by viewModel.timeData
    val threshold by viewModel.threshold

    LaunchedEffect(Unit) {
        viewModel.connect(context, deviceId, portNum, baudRate)
    }

    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            color = MaterialTheme.colorScheme.background
        )
    ){
        Text(text = "Crono Screen",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20.sp ,
            modifier = Modifier.padding(2.dp)
        )
        Row(modifier = Modifier.fillMaxSize()){
            Column(modifier = Modifier
                .padding(40.dp)
                .weight(0.5f)
                .align(Alignment.CenterVertically)
            )
            {

                Text(text = timeData , color = MaterialTheme.colorScheme.primary, fontSize = 50.sp , fontWeight = FontWeight.Bold , modifier = Modifier.padding(50.dp) )
                Row {
                    Text(text = threshold.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 30.sp ,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                    Slider(
                        value = threshold.toFloat(),
                        onValueChange = {viewModel.setThreshold(it.toInt())},
                        valueRange = 0f..255f
                    ) }
                Button(onClick = {
                    viewModel.send(context,"mensaje de prueba")
                }) {
                    Text(text = "Send Data")
                }
            }
            Column(modifier = Modifier
                .padding(20.dp)
                .background(color = MaterialTheme.colorScheme.background)
                .weight(0.3f)
                .fillMaxSize()
                .align(Alignment.CenterVertically)
                .verticalScroll(rememberScrollState()))
            {
                Text(text = "Crono Screen",color = MaterialTheme.colorScheme.primary)
                Text(text = "Received Data:",color = MaterialTheme.colorScheme.primary)
                Text(text = receivedData,color = MaterialTheme.colorScheme.primary)
            }
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

