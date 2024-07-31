package com.example.wheretoilet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wheretoilet.ui.theme.WhereToiletTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.flow.MutableStateFlow


/** 요청할 권한 **/
val permissions = arrayOf(
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_FINE_LOCATION
)
lateinit var arr: List<toiletData>


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arr = toiletArr(this.assets).processData()


        /*
        - 사용자가 권한 요청을 명시적으로 거부한 경우 true를 반환한다.
        - 사용자가 권한 요청을 처음 보거나, 다시 묻지 않음 선택한 경우, 권한을 허용한 경우 false를 반환한다.
        */
        val requestPermissionRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        permissIf(
            this,
            t = {
                Log.d("test5", "권한이 이미 존재합니다.")
                LocatePermiss.ok.value = true
                LocatePermiss.dialog.value = false
            }
        )




        setContent {
            WhereToiletTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box{
                        BottomSheet()

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
fun GoogleMap() {
    val ok = remember { LocatePermiss.ok }
    val busan = LatLng(35.137922, 129.055628)
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


    Box{
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = proper,
            uiSettings = uiSettings,
            onMapClick = {clickMarker.markerWindowView.value = false}
        ) {
            if(currentCameraPositionState.isMoving == true){
                btnView.value = false

                if (!LocatePermiss.ok.value){
                    permissIf(LocalContext.current, {
                        LocatePermiss.ok.value = true
                    })
                }

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

                Marker(
                    state = MarkerState(position = LatLng(it.weedo, it.gyeongdo)),
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

            }){Text("이 지역에서 다시 검색")}
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(){
    val markerData = clickMarker.markerData.collectAsState()
    val markerArr = clickMarker.markerArr.collectAsState()
    val markerWindowView = clickMarker.markerWindowView.collectAsState()
    val busan = LatLng(35.137922, 129.055628)
    val expanded = remember { mutableStateOf(false) }
    setMarkerArr(busan)

    val sheetState = rememberStandardBottomSheetState(
        confirmValueChange = { newState ->
            expanded.value = newState == SheetValue.Expanded
            true
        }
    )

    val bottomSheetState = rememberBottomSheetScaffoldState(sheetState)


    BottomSheetScaffold(
        sheetPeekHeight = 150.dp, //150으로 할 시, 확장 자체가 안되는 문제 발생 TODO
        scaffoldState = bottomSheetState,
        sheetContent = {
            if(markerWindowView.value){
                if(markerData.value != null){
                    Column(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        val data = markerData.value!!
                        if(expanded.value){
                            Text(data.name)
                            Text(data.division)
                            Text(data.streetAdd)
                            Text(data.openTimeDetail)
                        }
                        else {
                            Text("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                            Text(data.name)

                        }
                    }
                }
            }
            else {
                markerArr.value.forEach {
                    Text(it.toString())
                }
            }
        }
    ) {
        GoogleMap()
    }
}


@Composable
fun maxHeight(): Dp {
    return LocalConfiguration.current.screenHeightDp.dp
}


object LocatePermiss{
    val ok = mutableStateOf(false)
    val dialog = mutableStateOf(true)
}

object clickMarker{
    val markerData : MutableStateFlow<toiletData?> = MutableStateFlow(null)
    val markerArr : MutableStateFlow<List<toiletData>> = MutableStateFlow(listOf<toiletData>())
    val markerWindowView = MutableStateFlow(false)
}