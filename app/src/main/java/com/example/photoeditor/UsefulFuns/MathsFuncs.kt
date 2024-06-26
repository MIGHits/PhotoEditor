package com.example.photoeditor.UsefulFuns

import com.example.photoeditor.Cube3d.mat4x4
import com.example.photoeditor.Cube3d.vec3d
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun createColor(r:Int,g:Int,b:Int,a:Int):Int{
    return a shl 24 or (r shl 16) or (g shl 8) or (b)
}
fun multiply4x4MatVec(vI:vec3d,vO:vec3d,m: mat4x4){
    vO.x = vI.x * m.matrix[0][0] + vI.y * m.matrix[1][0] + vI.z * m.matrix[2][0] + vI.w * m.matrix[3][0]
    vO.y = vI.x * m.matrix[0][1] + vI.y * m.matrix[1][1] + vI.z * m.matrix[2][1] + vI.w * m.matrix[3][1]
    vO.z = vI.x * m.matrix[0][2] + vI.y * m.matrix[1][2] + vI.z * m.matrix[2][2] + vI.w *  m.matrix[3][2]
    vO.w = vI.x * m.matrix[0][3] + vI.y * m.matrix[1][3] + vI.z * m.matrix[2][3] + vI.w * m.matrix[3][3]

}
fun calculateLengthOfVec(vector:vec3d):Float{
    return sqrt(vector.x*vector.x + vector.y*vector.y + vector.z*vector.z)
}
fun normilazeVec(vector:vec3d,len:Float){
    vector.x /= len
    vector.y /= len
    vector.z /= len
}
