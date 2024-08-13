package com.example.wheretoilet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@Composable
fun MyGoogleMap(clickedData : toiletData?, forceRecenter: Boolean) {
    val ok = remember { LocatePermiss.ok } //어플의 위치 권한 설정 여부

    val proper = MapProperties(
        minZoomPreference = 10f,
        isMyLocationEnabled = ok.value
    )
    val uiSettings = MapUiSettings(myLocationButtonEnabled = ok.value)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busan, 15f)
    }

    val btnView = remember{ mutableStateOf(false) } //Map이 변경되었는지
    val currentLocate = remember{ mutableStateOf(busan) }   //현재 중심이 되는 위치

    LaunchedEffect(key1 = clickedData, key2 = forceRecenter){ //clickedData 혹은 forceRecenter가 변경되면 비동기 실행
        if(clickedData == null) return@LaunchedEffect

        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLng(LatLng(clickedData.weedo, clickedData.gyeongdo)),
            durationMs = 300
        )

    }

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


            arr.filter{
                it.weedo >= currentLocate.value.latitude - 0.01 &&
                        it.weedo <= currentLocate.value.latitude + 0.01 &&
                        it.gyeongdo >= currentLocate.value.longitude - 0.01 &&
                        it.gyeongdo <= currentLocate.value.longitude + 0.01
            }.forEach {
                val markerState = rememberMarkerState(position = LatLng(it.weedo, it.gyeongdo))

                if(clickedData?.num == it.num){
                    markerState.showInfoWindow()
                }


                Marker(
                    state = markerState,
                    title = it.name,
                    onClick = {_->
                        clickMarker.markerData.value = it
                        clickMarker.markerWindowView.value = true
                        false
                    }
                )

            }
        } //google map


        if(btnView.value == true){
            Button(onClick = {
                currentLocate.value = LatLng(cameraPositionState.position.target.latitude, cameraPositionState.position.target.longitude)
                setMarkerArr( currentLocate.value)

            }){ Text("이 지역에서 다시 검색") }
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