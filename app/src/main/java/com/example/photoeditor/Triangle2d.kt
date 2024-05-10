package com.example.photoeditor

class Triangle2d(points: Array<vec2d>) {
    val fPt : vec2d
    val sPt : vec2d
    val tPt : vec2d
    init {
        fPt = points[0]
        sPt = points[1]
        tPt = points[2]
    }
}
