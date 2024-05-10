package com.example.photoeditor.UsefulFuns

import com.example.photoeditor.Cube3d.Tria3d
import com.example.photoeditor.Cube3d.vec3d
import com.example.photoeditor.Triangle2d
import com.example.photoeditor.vec2d

fun convertVec3dTo2d(vec:vec3d):vec2d{
    return vec2d(vec.x.toInt(),vec.y.toInt())
}
fun convertTria3dTo2d(tria: Tria3d):Triangle2d{
    return Triangle2d(arrayOf(convertVec3dTo2d(tria.p[0]),convertVec3dTo2d(tria.p[1])))
}