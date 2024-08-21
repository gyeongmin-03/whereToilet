package com.example.wheretoilet

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    val markerData = clickMarker.markerData.collectAsState().value
    val markerWindowView = clickMarker.markerWindowView.collectAsState().value

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

    val currentLocate : MutableState<LatLng?> = remember { mutableStateOf(null) } //현재 실제 위치
    getLocationZip(context, currentLocate)


    BottomSheetScaffold(
        sheetPeekHeight = 200.dp,
        scaffoldState = bottomSheetState,
        sheetContent = {

            if(markerWindowView){
                if(markerData != null){
                    DetailPlace(markerData, expanded, currentLocate)
                }
            }
            else {
                PlaceList(context, currentLocate, forceRecenter, clickedCard, expanded)
            }
        }
    ) {
        MyGoogleMap(clickedCard.value, forceRecenter.value, currentLocate)
    }
}







@Composable
fun DetailPlace(data : toiletData, expanded : MutableState<Boolean>, currentLocate: MutableState<LatLng?>){
    Column(
        modifier = bottomSheetModifier
    ) {
        data.apply {
            if(expanded.value){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(name, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(vertical = 2.dp))
                    Text(division, fontSize = 16.sp, color = Color.LightGray, modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(horizontal = 5.dp, vertical = 5.dp))
                }

                HorizontalDivider()
                pText("도로명주소 : $streetAdd")

                HorizontalDivider()
                pText("개방시간 : $openTime")

                HorizontalDivider()
                pText("개방시간 상세 : $openTimeDetail")

                HorizontalDivider()
                pText("관리기관명 : $manageName")

                HorizontalDivider()
                pText("전화번호 : $phoneNum")

                HorizontalDivider()
                pText(" 화장실소유구분 : $possession")

                HorizontalDivider()
                pText("비상벨설치여부 : $emBell")

                HorizontalDivider()
                pText("비상벨 설치 장소 : $emBellLoc")

                HorizontalDivider()
                pText("화장실입구 CCTV 설치여부 : $cctv")

                HorizontalDivider()
                pText("기저귀교환대 유무 : $diaperChange")

                HorizontalDivider()
                pText("기저귀교환대장소 : $diaperChangeLoc")


            }
            else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start
                    ){
                        Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(vertical = 2.dp))
                        Text(division, fontSize = 16.sp, color = Color.LightGray, modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(horizontal = 5.dp, vertical = 5.dp))
                    }
                }

                if(currentLocate.value != null){
                    val curLocate = currentLocate.value!!
                    val txt = "현재 위치와의 거리 : "+getDistance(curLocate.latitude, curLocate.longitude, data.weedo, data.gyeongdo).toString()+" m"
                    Text(txt, fontSize = 20.sp, color = Color.Black)
                }

                HorizontalDivider()
                pText(streetAdd)

                HorizontalDivider()
                pText(openTime)

                HorizontalDivider()
                pText(openTimeDetail)
            }
        }
    }
}



/**
 * DetailPlace에서 사용되는 Text 컴포넌트의 속성 모음
 * */
@Composable
fun pText(str : String, modifier : Modifier = Modifier){
    Text(str, fontSize = 14.sp, modifier = modifier)
}



@Composable
fun PlaceList(context: Context, currentLocate: MutableState<LatLng?>, forceRecenter : MutableState<Boolean>, clickedCard: MutableState<toiletData?>, expanded: MutableState<Boolean>){
    val systemLocationEnalbe = remember { LocatePermiss.systemLocationEnalbe } //기기의 위치 설정 여부
    val markerArr = clickMarker.markerArr.collectAsState().value

    if(expanded.value){
        LazyColumn(
            modifier = bottomSheetModifier
        ){
            itemsIndexed(items = markerArr){_, item ->
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ){
                        Text(item.name)

                        if(currentLocate.value != null && systemLocationEnalbe.value && permissions.all{ ContextCompat.checkSelfPermission( context, it ) == PackageManager.PERMISSION_GRANTED }){
                            val curLocate = currentLocate.value!!
                            Text(getDistance(curLocate.latitude, curLocate.longitude, item.weedo, item.gyeongdo).toString()+" m")
                        }
                    }
                } //Card

                HorizontalDivider(modifier = Modifier.padding(2.dp))
            } //itemsIndexed
        } //LazyColumn
    }//if(expended.value)
    else {
        LazyRow(
            modifier = bottomSheetModifier

        ) {
            itemsIndexed(items = markerArr){_, item ->
                Card(
                    onClick = {
                        forceRecenter.value = !forceRecenter.value
                        clickedCard.value = item },
                    modifier = Modifier
                        .height(150.dp)
                        .width(150.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column {
                        Text(item.name)

                        if(currentLocate.value != null && systemLocationEnalbe.value && permissions.all{ ContextCompat.checkSelfPermission( context, it ) == PackageManager.PERMISSION_GRANTED }){
                            val curLocate = currentLocate.value!!
                            Text(getDistance(curLocate.latitude, curLocate.longitude, item.weedo, item.gyeongdo).toString()+" m")
                        }
                    }
                }
            } //itemsIndexed
        } //LazyRow
    } //else
}



@Composable
fun DistanceReButton(context: Context, currentLocate: MutableState<LatLng?>){
    Button(
        onClick = {
            getLocationZip(context, currentLocate)
        },
        modifier = Modifier
            .wrapContentHeight(),
    ) {
        Text("거리 갱신", modifier = Modifier.padding(2.dp))
    }
}