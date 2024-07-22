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
                    it[8],
                    it[9],
                    it[10],
                    it[11],
                    it[12],
                    it[13],
                    it[14],
                    it[15],
                    it[16],
                    it[17],
                    it[18].toDouble(),
                    it[19].toDouble(),
                    it[20],
                    if(it[21]=="Y") true else false,
                    it[22],
                    if(it[23]=="Y") true else false,
                    if(it[24]=="Y") true else false,
                    it[25]
                ))
            } catch (e: NumberFormatException){

            }
        }

        return arr
    }
}