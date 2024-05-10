package com.example.photoeditor.Cube3d

import com.example.photoeditor.vec2d


class Tria3d(points: Array<vec3d>,color:Int) {
    var p: Array<vec3d>
    var clr:Int
    init {
        p =  Array(3){points[0]}
        p[0] = points[0]
        p[1] = points[1]
        p[2] = points[2]
        clr = color
    }

}