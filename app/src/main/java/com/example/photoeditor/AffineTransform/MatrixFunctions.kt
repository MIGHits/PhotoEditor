package com.example.photoeditor.AffineTransform

import com.example.photoeditor.Cube3d.vec3d
import com.example.photoeditor.vec2d

//Создает минор матрицы, без p строки и q столбца
private fun getCofactor(matrix:Array<Array<Float>>, temp:Array<Array<Float>>, p:Int, q:Int, n:Int)
{
    var i = 0
    var j = 0
    for (row in 0..<n)
    {
        for (col in 0..<n)
        {
            if (row != p && col != q)
            {
                temp[i][j++] = matrix[row][col];

                if (j == n - 1)
                {
                    j = 0;
                    i++;
                }
            }
        }
    }
}
//Вычисляет определитель матрицы
fun det(matrix:Array<Array<Float>>, n:Int): Float{
    var d = 0.0f

    if (n == 1) return matrix[0][0]

    val temp = Array(n){Array(n){0.0f} }

    var sign = 1

    for (f in 0..<n)
    {
        getCofactor(matrix, temp, 0, f, n)
        d += sign * matrix[0][f] * det(temp, n - 1)

        sign = -sign
    }

    return d
}
fun vecSum(vec1:vec3d,vec2:vec3d):vec3d{
    return vec3d(vec1.x+vec2.x,vec1.y+vec2.y,vec1.z+vec2.z,0.0f);
}
fun vecDiff(vec1:vec3d,vec2:vec3d):vec3d{
    return vec3d(vec1.x-vec2.x,vec1.y-vec2.y,vec1.z-vec2.z,0.0f);
}
fun vecDiv(vec1:vec3d,k:Float):vec3d{
    return vec3d(vec1.x/k,vec1.y/k,vec1.z/k,vec1.w);
}
fun scalarProduct(vec1:vec3d,vec2:vec3d):Float{
    return vec1.x*vec2.x + vec1.y*vec2.y + vec1.z*vec2.z;
}
