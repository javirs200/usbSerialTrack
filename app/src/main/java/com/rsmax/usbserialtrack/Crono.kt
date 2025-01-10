package com.rsmax.usbserialtrack

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.usb.UsbManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

class CronoViewModel : ViewModel(), SerialInputOutputManager.Listener{

    private val writeWaitMilliseconds = 1000
    private var usbSerialPort: UsbSerialPort? = null
    private var usbIoManager: SerialInputOutputManager? = null
    private val _receivedData = mutableStateOf("")
    private val _timeData = mutableStateOf("0:00.000")
    private val _sessionName = mutableStateOf("")
    private val _threshold = mutableIntStateOf(50)
    private var connected : Boolean = false
    private var development : Boolean = false
    private val timesManager : TimesManager = TimesManager()
    var sessionName : State<String> = _sessionName
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
                development = true
                Toast.makeText(context, "USB device not found , enter in dev mode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewData(data: ByteArray) {
        val temString = String(data)
        if(temString.contains("time:")){
            val time = timesManager.convertTime(temString)
            _timeData.value = time.formated
            timesManager.addTime(time)
            _receivedData.value += time.formated
        }else{
            _receivedData.value += "Debug log : " + String(data)
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

    fun storeTimes(context: Context) {
        if(timesManager.storeTimes(context,development, sessionName.value)){
            Toast.makeText(context, "data stored successfully", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "something goes wrong in file storage", Toast.LENGTH_SHORT).show()
        }
    }

    fun setSessionName(sessionName: String) {
        this._sessionName.value = sessionName
    }

    fun getTimes(): ArrayList<Time> {
        return timesManager.getTimes()
    }

    fun getTopTime(): Time{
        return timesManager.getTopTime()
    }
}

@Composable
fun SessionNamePicker(cronoViewModel:CronoViewModel){
    val showDialog = remember { mutableStateOf(true) }
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "Session Name") },
            text = {
                Column {
                    OutlinedTextField(
                        value = cronoViewModel.sessionName.value,
                        onValueChange = { cronoViewModel.setSessionName(it) },
                        label = { Text("Enter session name") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    Button(onClick = { showDialog.value = true }) {
        Text("Set Session Name")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CronoScreen(deviceId: Int, portNum: Int, baudRate: Int) {
    val context = LocalContext.current
    val cronoViewModel: CronoViewModel = viewModel()
    val receivedData by cronoViewModel.receivedData
    val timeData by cronoViewModel.timeData
    val threshold by cronoViewModel.threshold
    val sessionName by cronoViewModel.sessionName

    LaunchedEffect(Unit) {
        cronoViewModel.connect(context, deviceId, portNum, baudRate)
    }

    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    SessionNamePicker(cronoViewModel)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            color = MaterialTheme.colorScheme.background
        )
    ){
        Text(text = "Crono Screen , Session : $sessionName",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20.sp ,
            modifier = Modifier.padding(start = 20.dp , top = 20.dp,end = 0.dp, bottom = 10.dp)
        )
        Row(modifier = Modifier.fillMaxSize()){
            Column(modifier = Modifier
                .padding(40.dp)
                .weight(0.5f)
                .align(Alignment.CenterVertically)
            )
            {
                Text(text = timeData ,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 100.sp ,
                    fontWeight = FontWeight.Bold ,
                    modifier = Modifier
                        .padding(20.dp)
                )

                Row {
                    Text(text = threshold.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 30.sp ,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                    Slider(
                        value = threshold.toFloat(),
                        onValueChange = {cronoViewModel.setThreshold(it.toInt())},
                        valueRange = 0f..255f
                    ) 
                }
                Row (modifier = Modifier.align(Alignment.CenterHorizontally)){
                    Button(onClick = {
                        cronoViewModel.send(context,"mensaje de prueba")
                    }) {
                        Text(text = "Send Data")
                    }
                    Spacer(
                        modifier = Modifier.padding(20.dp,0.dp)
                    )
                    Button(onClick = {
                        cronoViewModel.storeTimes(context)
                    }) {
                        Text(text = "Store times")
                    }
                }
            }
            var state by remember { mutableIntStateOf(0) }
            val titles = listOf("Tab 1", "Tab 2")
            Column(modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxSize()
                .padding(20.dp)
                .background(color = MaterialTheme.colorScheme.background)
                .weight(0.3f)
            )
            {
                PrimaryTabRow(selectedTabIndex = state) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = state == index,
                            onClick = { state = index },
                            text = {
                                Text(text = title, overflow = TextOverflow.Ellipsis)
                            }
                        )
                    }
                }
                if(state == 0){
                    Text(text = "Raw Log",color = MaterialTheme.colorScheme.primary)
                    Text(text = receivedData,color = MaterialTheme.colorScheme.primary)
                }else if(state == 1){
                    Text(text = "Fastest Lap : ${cronoViewModel.getTopTime().formated}",color = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier
                        .verticalScroll(rememberScrollState())) {
                        val times = cronoViewModel.getTimes()
                        for(t in times){
                            ListItem(
                                headlineContent = { Text(t.formated) },
                            )
                            HorizontalDivider()
                        }
                }
                }

            }

        }
    }

}

@Preview(
    device = "spec:parent=virtualPoco,orientation=landscape", apiLevel = 34
)
@Composable
fun CronoPreview(){
    CronoScreen(0, 0, 115200 )
}

