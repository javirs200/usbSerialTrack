package com.rsmax.usbserialtrack

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

data class Time(val formated: String, val timestamp: String)

class TimesManager{

    private val listTimes = ArrayList<Time>()

    fun addTime(formated: String, timestamp: String){
        listTimes.add(Time(formated,timestamp))
    }

    fun storeTimes(context: Context, development:Boolean, sessionName:String):Boolean{

        try {
            val appSpecificExternalDir = File(context.getExternalFilesDir(null), "$sessionName.txt")
            appSpecificExternalDir.printWriter().use { out ->
                if(development){
                    out.println("t1 de prueba")
                    out.println("t2 de prueba")
                    out.println("t3 de prueba")
                    out.println("t4 de prueba")
                }else {
                    for (t in listTimes) {
                        out.println(t)
                    }
                }
            }
            return true
        }catch(e:Exception){
            return false
        }
    }

    fun listTimeFiles(){
        TODO()
    }

    fun openTimeFile(){
        TODO()
    }
}

@Composable
fun TimesScreen() {
    val context = LocalContext.current
    Text(text = "Crono Screen",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20.sp ,
        modifier = Modifier.padding(2.dp)
    )
    Button(onClick = {
        Toast.makeText(context, "timer toast", Toast.LENGTH_SHORT).show()
    }) {
        Text(text = "make text")
    }
}

@Preview(
    device = "spec:parent=virtualPoco,orientation=landscape", apiLevel = 34
)
@Composable
fun TimeManagerPreview(){
    TimesScreen()
}