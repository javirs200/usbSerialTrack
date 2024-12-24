package com.rsmax.usbserialtrack

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

data class Time(val formated: String, val timestamp: String)

class TimesViewModel : ViewModel(){
    val listTimes = ArrayList<Time>();

    fun addTime(formated: String, timestamp: String){
        listTimes.add(Time(formated,timestamp));
    }

    fun addTime(time: Time){
        listTimes.add(time);
    }

    fun storeTimes(){
        // TODO Store times in filesystem
    }
}

@Composable
fun TimeManager(){
    val viewModel: TimesViewModel = viewModel()
    Button(onClick = {
        viewModel.storeTimes()
    }) {
        Text(text = "Store times")
    }
}