package com.rsmax.usbserialtrack

import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

data class Time(val formated: String, val timestamp: String)

class TimesViewModel : ViewModel(){
    val listTimes = ArrayList<Time>();

    fun addTime(formated: String, timestamp: String){
        listTimes.add(Time(formated,timestamp));
    }

    fun addTime(time: Time){
        listTimes.add(time);
    }

    fun storeTimes(timedata: String,development:Boolean){
        val file = File("prueba/file485.txt")
        file.printWriter().use { out ->
            if(development){
                out.println("t1 de prueba")
                out.println("t2 de prueba")
                out.println("t3 de prueba")
                out.println("t4 de prueba")
            }
            out.print(timedata)
        }
    }
}

@Composable
fun TimeManager(timedata: String, devModeActive: Boolean){
    val context = LocalContext.current
    val viewModel: TimesViewModel = viewModel()
    Button(onClick = {
        viewModel.storeTimes(timedata,devModeActive)
        Toast.makeText(context, "data Stored", Toast.LENGTH_SHORT).show()
    }) {
        Text(text = "Store times")
    }
}