package com.example.wheretoilet

import android.content.Context
import android.content.pm.PackageManager
import android.text.Layout.Alignment
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)
                    Text(division, fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(horizontal = 5.dp))
                }
                HorizontalDivider()
                Text(streetAdd, fontSize = 14.sp)

                HorizontalDivider()
                Row {
                    Text(openTime)
                    Text(" ", fontSize = 16.sp)
                    Text(openTimeDetail)
                }

                HorizontalDivider()
                Text(manageName)

                HorizontalDivider()
                Text(phoneNum)

                HorizontalDivider()
                Text(possession)

                HorizontalDivider()
                Text(emBell.toString())

                HorizontalDivider()
                Text(emBellLoc)

                HorizontalDivider()
                Text(cctv.toString())

                HorizontalDivider()
                Text(diaperChange.toString())

                HorizontalDivider()
                Text(diaperChangeLoc)





            }
            else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start
                    ){
                        Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)
                        Text(division, fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(horizontal = 5.dp))
                    }


                    if(currentLocate.value != null){
                        val curLocate = currentLocate.value!!
                        Text(getDistance(curLocate.latitude, curLocate.longitude, data.weedo, data.gyeongdo).toString()+" m")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(0.dp)

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ){
                    Text(item.name)

                    if(currentLocate.value != null && systemLocationEnalbe.value && permissions.all{ ContextCompat.checkSelfPermission( context, it ) == PackageManager.PERMISSION_GRANTED }){
                        val curLocate = currentLocate.value!!
                        Text(getDistance(curLocate.latitude, curLocate.longitude, item.weedo, item.gyeongdo).toString()+" m")
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(2.dp))
        }
    }
}



@Composable
fun DistanceReButton(context: Context, currentLocate: MutableState<LatLng?>){
    Button(
        onClick = {
            getLocationZip(context, currentLocate)
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Text("거리 갱신", modifier = Modifier.padding(2.dp))
    }
}