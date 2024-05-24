package com.example.photoeditor.Cube3d

import com.example.photoeditor.AffineTransform.AffineTransform
import com.example.photoeditor.UsefulFuns.findMax3
import com.example.photoeditor.UsefulFuns.findMin3
import kotlinx.coroutines.async
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

fun isPointInTriangle(x:Int,y:Int, triangle: Tria3d):Boolean{
    val a = triangle.p[0]
    val b = triangle.p[1]
    val c = triangle.p[2]
    //val aSide = (a.y - b.y)*p.x + (b.x - a.x)*p.y + (a.x*b.y - b.x*a.y)
    //val bSide = (b.y - c.y)*p.x + (c.x - b.x)*p.y + (b.x*c.y - c.x*b.y)
    //val cSide = (c.y - a.y)*p.x + (a.x - c.x)*p.y + (c.x*a.y - a.x*c.y)
    val aSide = (a.x - x) * (b.y - a.y) - (b.x - a.x) * (a.y - y)
    val bSide = (b.x - x) * (c.y - b.y) - (c.x - b.x) * (b.y - y)
    val cSide = (c.x - x) * (a.y - c.y) - (a.x - c.x) * (c.y - y)
    return (aSide >= 0 && bSide >= 0 && cSide >= 0 ) || (aSide < 0 && bSide < 0 && cSide < 0)
}
//withContext(Dispatchers.Default) {
//    val numCores = Runtime.getRuntime().availableProcessors()
//    val chunkSize = ceil(mesh.tris.size.toDouble() / numCores).toInt()
//
//    val deferredResults = (0 until numCores).map { core ->
//        async {
//            val startTris = core * chunkSize
//            val endTris = minOf(startTris + chunkSize, mesh.tris.size)
//
//
//
//        }
//    }
//    deferredResults.forEach { it.await() }
//}
fun findBox2D(tris: Tria3d, width:Int, height:Int):Box2D{
    val result = Box2D(
        vec3d(max(findMin3(tris.p[0].x,tris.p[1].x,tris.p[2].x),0.0f),
              max(findMin3(tris.p[0].y,tris.p[1].y,tris.p[2].y),0.0f),
            0.0f),
        vec3d(min(findMax3(tris.p[0].x,tris.p[1].x,tris.p[2].x),width.toFloat()),
              min(findMax3(tris.p[0].y,tris.p[1].y,tris.p[2].y),height.toFloat()),
            0.0f))
    return result
}
fun drawTriangle(pixels:IntArray, triangle: Tria3d, imgWidth:Int,imgHeight: Int, texture:IntArray, texWidth:Int){

    texturedTriangle(triangle,pixels,texture,imgWidth,imgHeight,texWidth)

}
fun texturedTriangle(tris:Tria3d, image: IntArray, texture:IntArray, imgWidth:Int,imgHeight:Int, texWidth:Int){
    var x1 = tris.p[0].x.toInt()
    var x2 = tris.p[1].x.toInt()
    var x3 = tris.p[2].x.toInt()
    var y1 = tris.p[0].y.toInt()
    var y2 = tris.p[1].y.toInt()
    var y3 = tris.p[2].y.toInt()
    var u1 = tris.t[0].x
    var u2 = tris.t[1].x
    var u3 = tris.t[2].x
    var v1 = tris.t[0].y
    var v2 = tris.t[1].y
    var v3 = tris.t[2].y
    var w1 = tris.t[0].w
    var w2 = tris.t[1].w
    var w3 = tris.t[2].w
    if (y2 < y1){
        y1=y2.also { y2 = y1 }
        x1=x2.also { x2 = x1 }
        u1=u2.also { u2 = u1 }
        v1=v2.also { v2 = v1 }
        w1=w2.also { w2 = w1 }
    }
    if (y3 < y1){
        y1=y3.also { y3 = y1 }
        x1=x3.also { x3 = x1 }
        u1=u3.also { u3 = u1 }
        v1=v3.also { v3 = v1 }
        w1=w3.also { w3 = w1 }
    }
    if (y3 < y2){
        y2=y3.also { y3 = y2 }
        x2=x3.also { x3 = x2 }
        u2=u3.also { u3 = u2 }
        v2=v3.also { v3 = v2 }
        w2=w3.also { w3 = w2 }
    }
    var dy1 = y2-y1
    var dx1 = x2-x1
    var dv1 = v2-v1
    var du1 = u2-u1
    var dw1 = w2 - w1

    val dy2 = y3-y1
    val dx2 = x3-x1
    val dv2 = v3-v1
    val du2 = u3-u1
    val dw2 = w3-w1

    var tex_u = 0.0f
    var tex_v = 0.0f
    var tex_w = 0.0f

    var dax_step = 0.0f
    var dbx_step = 0.0f
    var du1_step = 0.0f
    var du2_step = 0.0f
    var dv1_step = 0.0f
    var dv2_step = 0.0f
    var dw1_step = 0.0f
    var dw2_step = 0.0f

    if (dy1!=0) dax_step = dx1/(abs(dy1)).toFloat()
    if (dy2!=0) dbx_step = dx2/(abs(dy2)).toFloat()

    if (dy1!=0) du1_step = du1/(abs(dy1)).toFloat()
    if (dy1!=0) dv1_step = dv1/(abs(dy1)).toFloat()
    if (dy1!=0) dw1_step = dw1/(abs(dy1)).toFloat()

    if (dy2!=0) du2_step = du2/(abs(dy2)).toFloat()
    if (dy2!=0) dv2_step = dv2/(abs(dy2)).toFloat()
    if (dy2!=0) dw2_step = dw2/(abs(dy2)).toFloat()

    if (dy1 !=0){
        for(i in y1..y2){
            var ax = x1 + (i-y1).toFloat() * dax_step
            var bx = x1 + (i-y1).toFloat() * dbx_step

            //начальные позиции с текстуры
            var tex_su = u1 + (i-y1).toFloat() * du1_step
            var tex_sv = v1 + (i-y1).toFloat() * dv1_step
            var tex_sw = w1 + (i-y1).toFloat() * dw1_step

            //конечные позиции
            var tex_eu = u1 + (i-y1).toFloat() * du2_step
            var tex_ev = v1 + (i-y1).toFloat() * dv2_step
            var tex_ew = w1 + (i-y1).toFloat() * dw2_step

            if(ax > bx){
                ax = bx.also { bx = ax }
                tex_su = tex_eu.also { tex_eu = tex_su }
                tex_sv = tex_ev.also { tex_ev = tex_sv }
                tex_sw = tex_ew.also { tex_ew = tex_sw }
            }

            tex_u = tex_su
            tex_v = tex_sv
            tex_w = tex_sw

            val tstep = 1.0f / (bx-ax)
            var t = 0.0f

            for(j in ax.toInt()..<bx.toInt()){
                tex_u = (1.0f - t) * tex_su + t * tex_eu
                tex_v = (1.0f - t) * tex_sv + t * tex_ev
                tex_w = (1.0f - t) * tex_sw + t * tex_ew

                if(j < imgWidth && j > 0 && i <imgHeight && i > 0){
                    image[j + i*imgWidth] = texture[(tex_u/tex_w).toInt() + (tex_v/tex_w).toInt()*texWidth]
                }

                t+=tstep
            }
        }
    }
    dy1 = y3-y2
    dx1 = x3-x2
    dv1 = v3-v2
    du1 = u3-u2
    dw1 = w3 - w2

    if (dy1!=0) dax_step = dx1/(abs(dy1)).toFloat()
    if (dy2!=0) dbx_step = dx2/(abs(dy2)).toFloat()

    du1_step = 0.0f
    dv1_step = 0.0f
    dw1_step = 0.0f
    if (dy1!=0) du1_step = du1/(abs(dy1)).toFloat()
    if (dy1!=0) dv1_step = dv1/(abs(dy1)).toFloat()
    if (dy1!=0) dw1_step = dw1/(abs(dy1)).toFloat()


    for(i in y2..y3){
        var ax = x2 + (i-y2).toFloat() * dax_step
        var bx = x1 + (i-y1).toFloat() * dbx_step

        //начальные позиции с текстуры
        var tex_su = u2 + (i-y2).toFloat() * du1_step
        var tex_sv = v2 + (i-y2).toFloat() * dv1_step
        var tex_sw = w2 + (i-y2).toFloat() * dw1_step

        //конечные позиции
        var tex_eu = u1 + (i-y1).toFloat() * du2_step
        var tex_ev = v1 + (i-y1).toFloat() * dv2_step
        var tex_ew = w1 + (i-y1).toFloat() * dw2_step

        if(ax > bx){
            ax = bx.also { bx = ax }
            tex_su = tex_eu.also { tex_eu = tex_su }
            tex_sv = tex_ev.also { tex_ev = tex_sv }
            tex_sw = tex_ew.also { tex_ew = tex_sw }
        }

        tex_u = tex_su
        tex_v = tex_sv
        tex_w = tex_sw

        val tstep = 1.0f / (bx-ax)
        var t = 0.0f

        for(j in ax.toInt()..<bx.toInt()){
            tex_u = (1.0f - t) * tex_su + t * tex_eu
            tex_v = (1.0f - t) * tex_sv + t * tex_ev
            tex_w = (1.0f - t) * tex_sw + t * tex_ew

            if(j < imgWidth && j > 0 && i <imgHeight && i > 0){
                image[j + i*imgWidth] = texture[(tex_u/tex_w).toInt() + (tex_v/tex_w).toInt()*texWidth]
            }


            t+=tstep
        }
    }
}

