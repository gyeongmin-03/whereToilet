package com.example.wheretoilet

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.wheretoilet.ui.theme.WhereToiletTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.exp


/** 요청할 권한 **/
val permissions = arrayOf(
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_FINE_LOCATION
)
lateinit var arr: List<toiletData>
val busan = LatLng(35.137922, 129.055628)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arr = toiletArr(this.assets).processData() //전체 데이터 초기화


        /*
        - 사용자가 권한 요청을 명시적으로 거부한 경우 true를 반환한다.
        - 사용자가 권한 요청을 처음 보거나, 다시 묻지 않음 선택한 경우, 권한을 허용한 경우 false를 반환한다.
        */
        val requestPermissionRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        //어플의 위치 권한이 존재하는 지 확인
        permissIf(
            this,
            t = {
                Log.d("test5", "권한이 이미 존재합니다.")
                LocatePermiss.ok.value = true
                LocatePermiss.dialog.value = false
            }
        )

        LocatePermiss.systemLocationEnalbe.value = isEnableLocationSystem(this) //기기의 위치 설정으로 초기화
        Log.d("위치 설정", LocatePermiss.systemLocationEnalbe.value.toString())

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


@Composable
fun DetailPlace(data : toiletData, expanded : MutableState<Boolean>){
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 10.dp)
    ) {
        data.apply {
            if(expanded.value){
                Text(name)
                Text(division)
                Text(streetAdd)
                Text(openTimeDetail)
            }
            else {
                Row {
                    Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)
                    Text(" ", fontSize = 16.sp)
                    Text(division, fontSize = 12.sp, color = Color.LightGray)
                }
                HorizontalDivider()
                Text(streetAdd, fontSize = 14.sp)
                HorizontalDivider()
                Row {
                    Text(openTime)
                    Text(" ", fontSize = 16.sp)
                    Text(openTimeDetail)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(){
    val context = LocalContext.current

    val markerData = clickMarker.markerData.collectAsState()
    val markerArr = clickMarker.markerArr.collectAsState()
    val markerWindowView = clickMarker.markerWindowView.collectAsState()
    setMarkerArr(busan)

    val expanded = remember { mutableStateOf(false) } //바텀시트가 완전히 확장된 상태인지 여부

    val sheetState = rememberStandardBottomSheetState(
        confirmValueChange = { newState ->
            expanded.value = newState == SheetValue.Expanded
            true
        }
    )
    val bottomSheetState = rememberBottomSheetScaffoldState(sheetState)
    val forceRecenter = remember { mutableStateOf(false) } //card 클릭 이벤트 강제 실행을 위함

    val clickedCard : MutableState<toiletData?> = remember { mutableStateOf(null) } //클릭된 card

    val currentLocate : MutableState<LatLng?> = remember { mutableStateOf(null) } //현재 위치
    getLocationZip(context, currentLocate)

    val systemLocationEnalbe = remember { LocatePermiss.systemLocationEnalbe } //기기의 위치 설정 여부




    BottomSheetScaffold(
        sheetPeekHeight = 200.dp,
        scaffoldState = bottomSheetState,
        sheetContent = {

            if(markerWindowView.value){
                if(markerData.value != null){
                    val data = markerData.value!!
                    DetailPlace(data, expanded)
                }
            }
            else {
                PlaceList(context, currentLocate, markerArr, forceRecenter, clickedCard, systemLocationEnalbe)
            }
        }
    ) {
        MyGoogleMap(clickedCard.value, forceRecenter.value)
    }
}



@Composable
fun PlaceList(context: Context, currentLocate: MutableState<LatLng?>, markerArr : State<List<toiletData>>, forceRecenter : MutableState<Boolean>, clickedCard: MutableState<toiletData?>, systemLocationEnalbe : MutableState<Boolean>){
    LazyColumn{
        item {
            Row{
                Text("거리 갱신")
                Button(onClick = {
                    getLocationZip(context, currentLocate)
                    Log.d("작동은 하니?4", (currentLocate.value != null).toString()) //false

                }) {
                }
            }
        }
        itemsIndexed(items = markerArr.value){_, item ->
            Card(onClick = {
                forceRecenter.value = !forceRecenter.value
                clickedCard.value = item
            }) {
                Row{
                    Text(item.name)
                    Log.d("작동은 하니?1", (currentLocate.value != null).toString()) //false
                    Log.d("작동은 하니?2",(systemLocationEnalbe.value).toString())
                    Log.d("작동은 하니?3",(permissions.all{ ContextCompat.checkSelfPermission( context, it ) == PackageManager.PERMISSION_GRANTED }).toString())


                    if(currentLocate.value != null && systemLocationEnalbe.value && permissions.all{ ContextCompat.checkSelfPermission( context, it ) == PackageManager.PERMISSION_GRANTED }){
                        Log.d("작동은 하니?", "작동은 하니?") //작동 안하는 중

                        val curLocate = currentLocate.value!!
                        Text("  ")
                        Text(getDistance(curLocate.latitude, curLocate.longitude, item.weedo, item.gyeongdo).toString())
                    }
                }
            }
            HorizontalDivider()
        }
    }
}



@Composable
fun maxHeight(): Dp {
    return LocalConfiguration.current.screenHeightDp.dp
}


object LocatePermiss{
    val ok = mutableStateOf(false) //어플의 위치 권한 설정 여부
    val dialog = mutableStateOf(true)   //다이얼로그 표시 여부
    val systemLocationEnalbe = mutableStateOf(false) //기기의 위치 설정 여부
}

object clickMarker{
    val markerData : MutableStateFlow<toiletData?> = MutableStateFlow(null) //마커의 데이터
    val markerArr : MutableStateFlow<List<toiletData>> = MutableStateFlow(listOf<toiletData>()) //표시되는 마커 arr
    val markerWindowView = MutableStateFlow(false)  //마커의 윈도우를 보일것인가
}