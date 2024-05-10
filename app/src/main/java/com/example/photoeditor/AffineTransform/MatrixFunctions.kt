package com.example.photoeditor.AffineTransform

//Создает минор матрицы, без p строки и q столбца
private fun getCofactor(matrix:Array<Array<Int>>, temp:Array<Array<Int>>, p:Int, q:Int, n:Int)
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
fun det(matrix:Array<Array<Int>>, n:Int): Double{
    var d = 0.0

    if (n == 1) return matrix[0][0].toDouble()

    val temp = Array(n){Array(n){0} }

    var sign = 1

    for (f in 0..<n)
    {
        getCofactor(matrix, temp, 0, f, n)
        d += sign * matrix[0][f].toDouble() * det(temp, n - 1)

        sign = -sign
    }

    return d
}