package com.example.photoeditor.Cube3d

import com.example.photoeditor.vec2d


class Tria3d(p1:vec3d,p2:vec3d,p3:vec3d,t1:vec2d,t2:vec2d,t3:vec2d,color:Int) {
    var p: Array<vec3d>
    var t: Array<vec2d>
    var clr:Int
    init {
        p = arrayOf(p1,p2,p3)
        t = arrayOf(t1,t2,t3)
        clr = color
    }

}