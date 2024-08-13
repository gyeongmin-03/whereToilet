package com.example.wheretoilet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wheretoilet.ui.theme.WhereToiletTheme
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow


/** 요청할 권한 **/
val permissions = arrayOf(
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_FINE_LOCATION
)
lateinit var arr: List<toiletData>
val busan = LatLng(35.137922, 129.055628)
val bottomSheetModifier = Modifier.fillMaxHeight().padding(horizontal = 10.dp)

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

