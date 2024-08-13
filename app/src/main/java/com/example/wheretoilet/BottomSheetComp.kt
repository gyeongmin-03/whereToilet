package com.example.wheretoilet

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng






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

                    DetailPlace(data, expanded, context, currentLocate)
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
fun DetailPlace(data : toiletData, expanded : MutableState<Boolean>, context: Context, currentLocate: MutableState<LatLng?>){
    Column(
        modifier = bottomSheetModifier
    ) {
        DistanceReButton(context, currentLocate)

        data.apply {
            if(expanded.value){
                Row{
                    Text(name)

                    if(currentLocate.value != null){

                        val curLocate = currentLocate.value!!
                        Text("  ")
                        Text(getDistance(curLocate.latitude, curLocate.longitude, data.weedo, data.gyeongdo).toString())
                    }
                }
                Text(division)
                Text(streetAdd)
                Text(openTimeDetail)
            }
            else {
                Row {
                    Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)
                    Text(" ", fontSize = 16.sp)
                    Text(division, fontSize = 12.sp, color = Color.LightGray)

                    if(currentLocate.value != null){

                        val curLocate = currentLocate.value!!
                        Text("  ")
                        Text(getDistance(curLocate.latitude, curLocate.longitude, data.weedo, data.gyeongdo).toString())
                    }
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


@Composable
fun PlaceList(context: Context, currentLocate: MutableState<LatLng?>, markerArr : State<List<toiletData>>, forceRecenter : MutableState<Boolean>, clickedCard: MutableState<toiletData?>, systemLocationEnalbe : MutableState<Boolean>){
    LazyColumn(
        modifier = bottomSheetModifier
    ){
        item {
            DistanceReButton(context, currentLocate)
        }
        itemsIndexed(items = markerArr.value){_, item ->
            Card(
                onClick = {
                    forceRecenter.value = !forceRecenter.value
                    clickedCard.value = item },
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(0.dp)

            ) {
                Row{
                    Text(item.name)
                    Log.d("작동은 하니?1", (currentLocate.value != null).toString())
                    Log.d("작동은 하니?2",(systemLocationEnalbe.value).toString())
                    Log.d("작동은 하니?3",(permissions.all{ ContextCompat.checkSelfPermission( context, it ) == PackageManager.PERMISSION_GRANTED }).toString())


                    if(currentLocate.value != null && systemLocationEnalbe.value && permissions.all{ ContextCompat.checkSelfPermission( context, it ) == PackageManager.PERMISSION_GRANTED }){
                        Log.d("작동은 하니?", "작동은 하니?")

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
fun DistanceReButton(context: Context, currentLocate: MutableState<LatLng?>){
    Row{
        Text("거리 갱신")
        Button(onClick = {
            getLocationZip(context, currentLocate)
            Log.d("작동은 하니?4", (currentLocate.value != null).toString()) //false

        }) {
        }
    }
}