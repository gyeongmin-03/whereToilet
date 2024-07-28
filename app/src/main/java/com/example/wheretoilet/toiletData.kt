package com.example.wheretoilet

data class toiletData(
    val num : Int, //번호 ..5226
    val division : String, //구분(간이, 개방, 공중, 이동)
    val name : String, //화장실명
    val streetAdd : String, //도로명주소
    val address : String, //지번주소
    val maleBigCount : String, //남성용-대변기수
    val maleSmCount : String, //남성용-소변기수
    val disMaleBigCount : String, //남성용-장애인용대변기수
    val disMaleSmCount : String, //남성용-장애인용소변기수
    val chileMaleBigCount : String, //남성용-어린이용대변기수
    val chileMaleSmCount : String, //남성용-어린이용소변기수
    val famBigCount : String, //여성용-대변기수
    val disFamBigCount : String, //여성용-장애인용대변기수
    val chileFamBigCount : String, //여성용-어린이용대변기수
    val manageName : String, //관리기관명
    val phoneNum : String, //전화번호
    val openTime : String, //개방시간
    val openTimeDetail : String, //개방시간 상세
    val weedo : Double, //위도
    val gyeongdo : Double, //경도
    val possession : String, //화장실소유구분
    val emBell : Boolean, //비상벨설치여부
    val emBellLoc : String, //비상벨 설치 장소
    val cctv : Boolean, //화장실입구 CCTV 설치여부
    val diaperChange : Boolean, //기저귀교환대 유무
    val diaperChangeLoc : String //기저귀교환대장소
)