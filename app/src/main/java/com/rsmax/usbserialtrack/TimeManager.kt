package com.rsmax.usbserialtrack

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.lang.String.format
import java.util.Locale

data class Time(val formated: String, val timestamp: Int)

class TimesManager{

    private val listTimes = ArrayList<Time>()
    private var topTime :Time = Time("", Int.MAX_VALUE)

    fun addTime(time : Time){
        listTimes.add(time)
        if(time.timestamp < topTime.timestamp){
            topTime = time
        }
    }

    fun getTopTime():Time{
        return topTime
    }

    fun getTimes() : ArrayList<Time>{
        return listTimes;
    }

    fun convertTime(temString:String):Time{
        val aux = temString.substringAfter("time:").trim()
        val millis = aux.toInt()
        val minutes = (millis / 60000) % 60
        val seconds = (millis / 1000) % 60
        val milliseconds = millis % 1000
        val formattedTime = format(Locale.ROOT,"%02d:%02d.%03d", minutes, seconds, milliseconds)
        return Time(formattedTime, millis)
    }

    fun storeTimes(context: Context, development:Boolean, sessionName:String):Boolean{
        try {
            val appSpecificExternalDir = File(context.getExternalFilesDir(null), "$sessionName.txt")
            appSpecificExternalDir.printWriter().use { out ->
                if(development){
                    out.println("time:0000")
                }else {
                    for (t in listTimes) {
                        out.println("time:${t.timestamp}")
                    }
                }
            }
            return true
        }catch(e:Exception){
            return false
        }
    }

    fun listTimeFiles(context: Context):ArrayList<String>{
        val list = ArrayList<String>()
        // get all files in the appSpecificExternalDir directory
        try {
            File(context.getExternalFilesDir(null), "").listFiles()?.forEach {
                list.add(it.name)
            }
            return list
        }catch(e:Exception){
            return list
        }
    }

    fun openTimes(context:Context,fileName:String): ArrayList<Time> {
        val times :ArrayList<Time> = ArrayList()
        val timeFile = File(context.getExternalFilesDir(null),fileName)
        timeFile.forEachLine {
            times.add(convertTime(it))
        }
        return times
    }
}

@Composable
fun TimesScreen() {
    val context = LocalContext.current
    val timeManager = TimesManager()
    val fileSelected = remember { mutableStateOf("") }
    val showList = remember { mutableStateOf(true) }

    val timeFiles = timeManager.listTimeFiles(context)
    
    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            color = MaterialTheme.colorScheme.background
        )
    ){
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)){
            Text(text = "Time Screen ${fileSelected.value}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp ,
                modifier = Modifier.padding(start = 20.dp , top = 10.dp,end= 0.dp, bottom = 0.dp)
            )
            if(showList.value){
                for (timeFileName in timeFiles){
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                Toast
                                    .makeText(context, "$timeFileName selected", Toast.LENGTH_SHORT)
                                    .show()
                                fileSelected.value = timeFileName
                                showList.value = false
                            }
                    ){
                        Text(text = timeFileName,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp ,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }else{
                val times:ArrayList<Time> = timeManager.openTimes(context,fileSelected.value)
                times.forEachIndexed{ index , element ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ){
                        Row{
                            Text(text = index.toString(),
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 20.sp ,
                                modifier = Modifier.padding(2.dp)
                            )
                            Text(text = element.formated,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp ,
                                modifier = Modifier.padding(2.dp)
                            )
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
fun TimeManagerPreview(){
    TimesScreen()
}