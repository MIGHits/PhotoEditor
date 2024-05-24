package com.example.photoeditor.Cube3d

class mat4x4 {
    val matrix = Array(4) { FloatArray(4) { 0.0f } }
    init{
        matrix[0][0] = 1.0f
        matrix[1][1] = 1.0f
        matrix[2][2] = 1.0f
        matrix[3][3] = 1.0f
    }
}