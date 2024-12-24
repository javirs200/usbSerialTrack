package com.rsmax.usbserialtrack

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

data class Driver(val Name: String, val DeviceMac: String)

class DriversViewModel : ViewModel(){
    private val listDrivers = ArrayList<Driver>();

    fun addDriver(name: String, deviceMac: String){
        listDrivers.add(Driver(name,deviceMac));
    }

    fun addDriver(d : Driver){
        listDrivers.add(d);
    }

    fun removeDriver(name: String){
        listDrivers.removeIf({x-> x.Name == name})
    }

    fun isEmpty():Boolean{
        return listDrivers.isEmpty();
    }

    fun getListDrivers(): ArrayList<Driver> {
        return listDrivers;
    }
}

@Composable
fun DriversManager(){
    val viewModel: DriversViewModel = viewModel()
    if (viewModel.isEmpty()) {

    }else{
        Button(onClick = {

        }) {
            Text(text = "Store times")
        }
    }
}

@Composable
fun MinimalDialog(onDismissRequest: () -> Unit, Drivers:ArrayList<Driver>) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            for (d in Drivers){
                Text(
                    text = d.Name,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun AddNewDriverDialog(onDismissRequest: () -> Unit, Drivers:ArrayList<Driver>) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            // TODO basic imput form requesting name and scn the car after x Seconds
        }
    }
}
