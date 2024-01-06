package com.example.ortalamahiztakip


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.ortalamahiztakip.ui.theme.OrtalamaHizTakipTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainViewModel : ViewModel() {
    var speed = mutableStateOf<Float>(0.0f)
    var gidilenYol = mutableStateOf<Double>(0.0)
    var kalanMesafe = mutableStateOf("0")
    var distance = mutableStateOf("100000")
    var maxSpeed = mutableStateOf("0")
    var speedLimit = mutableStateOf("110")
    var startDate = mutableStateOf<Date>(Date())
    var actualTimeStamp = mutableStateOf<Long>(10)

    var finishDateTs = mutableStateOf<Long>(0)

    var tempDateTs = mutableStateOf<Long>(0)

    var lastLat = mutableStateOf<Double>(0.0)
    var lastLong = mutableStateOf<Double>(0.0)
    fun calculateFinishDate() {
        val startDateTs = startDate.value.time
        finishDateTs.value =
            (startDateTs + ((distance.value.toDouble() / 1000.0 / speedLimit.value.toDouble()) * 3600.0 * 1000.0)).toLong()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun calculateMaxSpeed() {
        val date1 = Calendar.getInstance().time.time

        val diffSeconds = (finishDateTs.value - date1) / 1000
        maxSpeed.value =
            ((distance.value.toDouble() - gidilenYol.value) / (diffSeconds.toDouble() / 3600.0) / 1000.0).toString()
        kalanMesafe.value = (distance.value.toDouble() - gidilenYol.value).toString()
    }
}

class MainActivity : ComponentActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2
    private val mainViewModel: MainViewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OrtalamaHizTakipTheme {
                Column {
                    InfoRow(mainViewModel = mainViewModel)
                    Greeting()
                    Configurations(mainViewModel = mainViewModel)
                }
            }
        }

    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = (Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta))))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    private fun getLocation() {
        mainViewModel.startDate.value = Calendar.getInstance().time
        mainViewModel.calculateFinishDate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onLocationChanged(location: Location) {
        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        if (mainViewModel.lastLat.value != 0.0 && mainViewModel.lastLong.value != 0.0) {
            mainViewModel.gidilenYol.value += distance(
                location.latitude,
                location.longitude,
                mainViewModel.lastLat.value,
                mainViewModel.lastLong.value
            ) * 1609.34
        }
        mainViewModel.speed.value = location.speed
        mainViewModel.calculateMaxSpeed()
        mainViewModel.lastLat.value = location.latitude
        mainViewModel.lastLong.value = location.longitude

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }

    @Composable
    fun InfoRow(mainViewModel: MainViewModel) {
        Column(
            Modifier
                .background(Color.Green)
                .fillMaxWidth()
        ) {
            Row(
                Modifier
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                Column(Modifier.fillMaxWidth(0.4f))
                {
                    Text(
                        text = "Çıkış Zamanı :"
                    )
                }
                Column(Modifier.fillMaxWidth(0.6f))
                {
                    Text(
                        text = "${convertLongToTime(mainViewModel.finishDateTs.value)}"
                    )
                }
            }

            Row(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(0.4f),
                    verticalArrangement = Arrangement.Center
                )
                {
                    Text(
                        text = "Maksimum Hız:"
                    )
                }
                Column(
                    modifier = Modifier.weight(0.4f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Text("${mainViewModel.maxSpeed.value}", fontSize = 30.sp)
                }
                Column(
                    modifier = Modifier
                        .weight(0.2f)
                        .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Text("km/s", fontSize = 25.sp)
                }
            }

            Row(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.4f))
                {
                    Text(
                        text = "Mevcut Hız :"
                    )
                }
                Column(modifier = Modifier.fillMaxWidth(0.6f))
                {
                    Text(
                        text = "${mainViewModel.speed.value}"
                    )
                }
            }
        }
    }

    @Composable
    fun Greeting() {
        Column(
            Modifier
                .background(Color.Green)
                .fillMaxWidth()
        ) {
            Row(
                Modifier
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                Button(onClick = {
                    getLocation()
                }, modifier = Modifier.fillMaxWidth())
                {
                    Text("Takibe Başla")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Configurations(mainViewModel: MainViewModel) {
        var unit by remember { mutableStateOf("km") }
        var vehicleType by remember { mutableStateOf("Otomobil") }
        var roadType by remember { mutableStateOf("Karayolu") }
        Column() {
            Row() {
                Column(
                    modifier = Modifier
                        .weight(0.2f)
                        .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mesafe")
                }
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = mainViewModel.distance.value,
                        onValueChange = {
                            if (it.isEmpty()) {
                                mainViewModel.distance.value = "0"
                            } else {
                                mainViewModel.distance.value = when (it.toDoubleOrNull()) {
                                    null -> mainViewModel.distance.value //old value
                                    else -> it   //new value
                                }
                            }
                            mainViewModel.calculateFinishDate()
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(0.2f)
                        .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("metre")
                }
            }
            Row() {
                Column(
                    modifier = Modifier
                        .weight(0.2f)
                        .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Hız Sınırı")
                }
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = mainViewModel.speedLimit.value,
                        onValueChange = {
                            if (it.isEmpty()) {
                                mainViewModel.speedLimit.value = "0"
                            } else {
                                mainViewModel.speedLimit.value = when (it.toDoubleOrNull()) {
                                    null -> mainViewModel.speedLimit.value //old value
                                    else -> it   //new value
                                }
                            }
                            mainViewModel.calculateFinishDate()
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

    }


    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        OrtalamaHizTakipTheme {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Column(
                    Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                ) {
                    InfoRow(mainViewModel = mainViewModel)
                    Greeting()
                    Configurations(mainViewModel = mainViewModel)
                }
            }
        }
    }

}

