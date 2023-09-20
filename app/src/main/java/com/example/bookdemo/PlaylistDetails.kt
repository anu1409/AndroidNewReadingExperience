package com.example.bookdemo

data class PlaylistDetails(
    val frameDetails: ArrayList<Frames>
)

data class Frames(
    val pageIndex:Int,
    val startTime:Double,
    val endTime:Double
)



