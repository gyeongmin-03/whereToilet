package com.example.wheretoilet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@Composable
fun MyGoogleMap(clickedData : toiletData?, forceRecenter: Boolean, currentLocate: MutableState<LatLng?>) {
    val ok = remember { LocatePermiss.ok } //어플의 위치 권한 설정 여부
    val context = LocalContext.current
    val proper = MapProperties(
        minZoomPreference = 10f,
        isMyLocationEnabled = ok.value
    )
    val uiSettings = MapUiSettings(myLocationButtonEnabled = ok.value)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busan, 15f)
    }

    val btnView = remember{ mutableStateOf(false) } //Map이 변경되었는지
    val currentCameraLocate = remember{ mutableStateOf(busan) }   //현재 카메라 위치

    LaunchedEffect(key1 = clickedData, key2 = forceRecenter){ //clickedData 혹은 forceRecenter가 변경되면 비동기 실행
        if(clickedData == null) return@LaunchedEffect

        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLng(LatLng(clickedData.weedo, clickedData.gyeongdo)),
            durationMs = 300
        )
    }


    val markers = clickMarker.markerArr.collectAsState().value

    Box{
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = proper,
            uiSettings = uiSettings,
            onMapClick = {
                clickMarker.markerWindowView.value = false
            }
        ) {
            if(currentCameraPositionState.isMoving == true){
                btnView.value = false
            }
            else { //currentCameraPositionState.isMoving == false
                btnView.value = true
            }

            markers.forEach { markerData ->
                val markerState = MarkerState(position = LatLng(markerData.weedo, markerData.gyeongdo))

                Marker(
                    state = markerState,
                    title = markerData.name,
                    onClick = {
                        clickMarker.markerData.value = markerData
                        clickMarker.markerWindowView.value = true
                        false
                    }
                )
            }



        } //google map


        if(btnView.value == true){
            Column {
                Button(onClick = {
                    currentCameraLocate.value = LatLng(cameraPositionState.position.target.latitude, cameraPositionState.position.target.longitude)
                    setMarkerArr( currentCameraLocate.value )
                }){ Text("이 지역에서 다시 검색") }

                DistanceReButton(context, currentLocate)
            }
        }
    } // box
}

fun setMarkerArr(currentLocate : LatLng){
    clickMarker.markerArr.value = arr.filter{
        it.weedo >= currentLocate.latitude - 0.01 &&
                it.weedo <= currentLocate.latitude + 0.01 &&
                it.gyeongdo >= currentLocate.longitude - 0.01 &&
                it.gyeongdo <= currentLocate.longitude + 0.01
    }
}