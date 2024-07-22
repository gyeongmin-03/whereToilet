package com.example.wheretoilet

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.wheretoilet.ui.theme.WhereToiletTheme
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*









/** 요청할 권한 **/
val permissions = arrayOf(
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_FINE_LOCATION
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arr = toiletArr(this.assets).processData()


        /*
        - 사용자가 권한 요청을 명시적으로 거부한 경우 true를 반환한다.
        - 사용자가 권한 요청을 처음 보거나, 다시 묻지 않음 선택한 경우, 권한을 허용한 경우 false를 반환한다.
        */
        val requestPermissionRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)


        if (permissions.all {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            Log.d("test5", "권한이 이미 존재합니다.")
            LocatePermiss.ok.value = true
        }
        if(LocatePermiss.ok.value == true) LocatePermiss.dialog.value = false

        setContent {
            WhereToiletTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box{
                        GoogleMap(arr)

                        val dialog = remember { LocatePermiss.dialog }

                        if(dialog.value){
                            PermissDialog(requestPermissionRationale)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GoogleMap(arr:  List<toiletData>) {
//    val ok = remember { mutableStateOf(LocatePermiss.ok) }
    val rec = remember{ mutableStateOf(0)}
    Log.d("구글맵이 리컴퍼블됨", "구글맵이 리컴퍼저블 됨")
    Toast.makeText(LocalContext.current, "${rec.value} 번 리컴퍼블됨", Toast.LENGTH_SHORT).show()

    val ok = remember { LocatePermiss.ok }
    val busan = LatLng(35.137922, 129.055628)
    val proper = MapProperties(
        minZoomPreference = 10f,
        isMyLocationEnabled = ok.value
    )
    val context = LocalContext.current

    var uiSettings = MapUiSettings(myLocationButtonEnabled = ok.value)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busan, 15f)
    }
    val btnView = remember{ mutableStateOf(false) } //Map이 변경되었는지
    val refresh = remember{ mutableStateOf(false) } //refresh가능한 상태인지
    val markerView = remember{ mutableStateOf(false) } //마커가 보이는 상태

    var latitude : Double = 0.0
    var longitude : Double = 0.0
    val currentLocate = remember{ mutableStateOf( LatLng(latitude, longitude)) }

    /*
    * 어차피 카메라 isMoving 때마다 리컴퍼지션 된다면,
    * 굳이 주기적으로 위치를 불러올 필요가 없을지도?
    * */
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                // 현재 위치 처리
                currentLocate.value = LatLng(location.latitude, location.longitude)
                Log.d("현재 위치", "${currentLocate.value}")
            }
        }
    }

    val locationRequest = LocationRequest.Builder(5*1000L).setMinUpdateIntervalMillis(3*1000L).build()



    Box{
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = proper,
            uiSettings = uiSettings
        ) {
            if(currentCameraPositionState.isMoving == true){
                btnView.value = false
                markerView.value = false
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
            else { //currentCameraPositionState.isMoving == false
                btnView.value = true
                if(currentCameraPositionState.position.zoom < 13){
                    //지도를 확대시켜주세요
                    refresh.value = false
                }
                else {
                    //이 지역에서 다시 검색 버튼
                    refresh.value = true
                }
            }

            if(markerView.value == true){
                arr.filter{
                    it.weedo >= currentCameraPositionState.position.target.latitude - 0.01 &&
                            it.weedo <= currentCameraPositionState.position.target.latitude + 0.01 &&
                            it.gyeongdo >= currentCameraPositionState.position.target.longitude - 0.01 &&
                            it.gyeongdo <= currentCameraPositionState.position.target.longitude + 0.01
                }.forEach {
                    Marker(
                        state = MarkerState(position = LatLng(it.weedo, it.gyeongdo)),
                    )
                }
            }

            if(proper.isMyLocationEnabled && permissions.all{ ContextCompat.checkSelfPermission( context, it ) == PackageManager.PERMISSION_GRANTED }){
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                DrawCircle(currentLocate)   //이건 지금 작동이 안되나?
            }

        } //google map

        if(btnView.value == true){
            if(refresh.value == true){
                Button(onClick = {
                    markerView.value = true


                    val a = permissions.all {
                        ContextCompat.checkSelfPermission(
                            context,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }

                }){Text("이 지역에서 다시 검색")}
            }
            else {
                Button(onClick = { cameraPositionState.move(CameraUpdateFactory.zoomTo(15f)) }){ Text("지도를 확대시켜주세요")}
            }
        }
    } // box
}


@Composable
fun DrawCircle(
    location: MutableState<LatLng>, // 현재 위치를 보관하는 상태
) {
    Log.d("DrawCircle 현재 위치", "${location}")
    //원 그리기
    Circle(
        center = location.value,
        clickable = false,
        radius = 10.0,
        strokeColor = Color.Red,
        strokeWidth = 1.0f,
        visible = true,
    )
}



object LocatePermiss{
    val ok = mutableStateOf(false)
    val dialog = mutableStateOf(true)
}