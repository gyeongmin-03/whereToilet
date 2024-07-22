package com.example.wheretoilet

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat


@Composable
fun PermissDialog(requestPermissionRationale : Boolean){
    val context = LocalContext.current

    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        /** 권한 요청시 동의 했을 경우 **/
        if (areGranted) {
            Log.d("test5", "권한이 동의되었습니다.")
            LocatePermiss.ok.value = true
        }
        /** 권한 요청시 거부 했을 경우 **/
        else {
            Log.d("test5", "권한이 거부되었습니다.")
            LocatePermiss.ok.value = false
//            Toast.makeText(LocalContext.current, "위치 권한 거부 시 현재 위치 주변 화장실을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
        }
    } //삼성에서 지원하는 기본 다이얼로그 선택시

    val dialogFalse = { LocatePermiss.dialog.value = false }
    val properties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
    )

    AlertDialog(
        onDismissRequest = dialogFalse,
        confirmButton = { Button(onClick = {
            checkAndRequestPermissions(
                requestPermissionRationale,
                context,
                permissions,
                launcherMultiplePermissions
            )
            dialogFalse.invoke()
        }
        ){Text("설정")} },
        dismissButton = { Button(onClick = dialogFalse){Text("취소")} },
        icon = { Icon(painter = painterResource(id = R.drawable.location), contentDescription = "location_icon") },
        title = { Text("위치 권한 설정") },
        text = { Text("위치 권한을 설정하지 않으면,\n 내 주변 위치 탐색을 할 수 없습니다.") },
        shape = RoundedCornerShape(20.dp),
        properties = properties
    )
}



fun checkAndRequestPermissions(
    requestPermissionRationale: Boolean,
    context: Context,
    permissions: Array<String>,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
) {

    /** 권한이 이미 있는 경우 **/
    if (permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }) {
        Log.d("test5", "권한이 이미 존재합니다.")
        LocatePermiss.ok.value = true
    }
    /** 권한이 없는 경우 **/
    else {
        val sharedPreferences = context.getSharedPreferences("MAIN", ComponentActivity.MODE_PRIVATE)
        val firstRequest = sharedPreferences.getBoolean("FIRST_REQUEST", true)

        /**첫 요청이거나, 한 번 명시적 거절 했을 경우**/
        if(firstRequest || requestPermissionRationale){
            launcher.launch(permissions)
            sharedPreferences.edit().putBoolean("FIRST_REQUEST", false).apply()
            Log.d("test5", "권한을 요청하였습니다.")
        }
        /**두 번 이상 거절했었던 경우**/
        else{
            val packageName = "com.example.wheretoilet"
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            context.startActivity(intent)
            Toast.makeText(context, "권한 → 위치를 활성화 해주세요", Toast.LENGTH_LONG).show()
        }
    }
}