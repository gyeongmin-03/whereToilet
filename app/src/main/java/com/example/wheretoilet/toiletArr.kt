package com.example.wheretoilet

import android.content.res.AssetManager
import com.opencsv.CSVReader
import java.io.InputStreamReader

class toiletArr(assets : AssetManager) {
    val filePath = assets.open("toilet.csv")
    val fileReader = CSVReader(InputStreamReader(filePath))
    val allContent = fileReader.readAll()
    val arr = mutableListOf<toiletData>()


    fun processData() : List<toiletData> {
        allContent.forEach {
            try {
                arr.add(toiletData(
                    it[0].toInt(),
                    it[1],
                    it[2],
                    it[3],
                    it[4],
                    it[5],
                    it[6],
                    it[7],
                    it[8].toDouble(),
                    it[9].toDouble(),
                    it[10],
                    if(it[11]=="Y") true else false,
                    it[12],
                    if(it[13]=="Y") true else false,
                    if(it[14]=="Y") true else false,
                    it[15]
                ))
            } catch (e: NumberFormatException){

            }
        }

        return arr
    }
}