package com.example.photoeditor.Cube3d

import android.content.Context
import android.graphics.Bitmap
import com.example.photoeditor.AffineTransform.det
import com.example.photoeditor.AffineTransform.scalarProduct
import com.example.photoeditor.AffineTransform.vecDiff
import com.example.photoeditor.AffineTransform.vecDiv
import com.example.photoeditor.UsefulFuns.calculateLengthOfVec
import com.example.photoeditor.UsefulFuns.createColor
import com.example.photoeditor.UsefulFuns.multiply4x4MatVec
import com.example.photoeditor.UsefulFuns.normilazeVec
import com.example.photoeditor.vec2d
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Scene3D {
    var mesh:Mesh = Mesh()
    val identityMatrix = mat4x4()
    val matProj = mat4x4()
    val matYRot = mat4x4()
    val matXRot = mat4x4()
    val matZRot = mat4x4()
    val midCorrection = mat4x4()
    lateinit var vCamera:vec3d
    fun createCamera(pos:vec3d)
    {
        vCamera = pos
    }
    fun projectionMatrix(fNear:Float, fFar:Float,fFov:Float,fAspectRatio:Float){
        val fFovRad = 1.0f / tan(fFov * 0.5f/180.0f * 3.14159f)

        matProj.matrix[0][0] = fAspectRatio * fFovRad
        matProj.matrix[1][1] = fFovRad
        matProj.matrix[2][2] = fFar / (fFar - fNear)
        matProj.matrix[3][2] = (-fFar * fNear) / (fFar - fNear)
        matProj.matrix[2][3] = 1.0f
        matProj.matrix[3][3] = 0.0f
    }
    fun loadMesh(obj: Array<Tria3d>){
        midCorrection.matrix[3][0] = -0.5f
        midCorrection.matrix[3][1] = -0.5f
        midCorrection.matrix[3][2] = -0.5f
        mesh.tris = Array(obj.size) { Tria3d(
            vec3d(0.0f,0.0f,0.0f,1.0f),
            vec3d(0.0f,0.0f,0.0f,1.0f),
            vec3d(0.0f,0.0f,0.0f,1.0f),
            vec2d(0.0f,0.0f),
            vec2d(0.0f,0.0f),
            vec2d(0.0f,0.0f)
        )  }
        for(i in 0..<obj.size){
            mesh.tris[i] = applyTransformMatrixToTria(obj[i],midCorrection)
        }
        //obj = applyTransformMatrixToTria(mesh.tris[i],midCorrection)

    }
    fun createRotationMatrix(axis:Char,angle:Float){
        when(axis){
            'x' ->{
                matXRot.matrix[1][1] = cos(angle)
                matXRot.matrix[1][2] = -sin(angle)
                matXRot.matrix[2][2] = cos(angle)
                matXRot.matrix[2][1] = sin(angle)
            }
            'y'->{
                matYRot.matrix[0][0] = cos(angle)
                matYRot.matrix[0][2] = sin(angle)
                matYRot.matrix[2][0] = -sin(angle)
                matYRot.matrix[2][2] = cos(angle)
            }
            'z'->{
                matZRot.matrix[0][0] = cos(angle)
                matZRot.matrix[1][0] = sin(angle)
                matZRot.matrix[0][1] = -sin(angle)
                matZRot.matrix[1][1] = cos(angle)
            }

        }
    }
    fun drawMesh(image: Bitmap,texture: IntArray,texWidth:Int){

        val pixels = IntArray(image.width*image.height)
        image.getPixels(pixels,0,image.width,0,0,image.width,image.height)

        //Виды треугольников, которые получатся после преобразований по матрицам
        var newTriaRotatedX:Tria3d
        var newTriaInCameraFOV:Tria3d
        //Линии для нахождения нормалей
        var line1:vec3d
        var line2:vec3d
        var normal:vec3d
        for (i in 0..<mesh.tris.size){

            //mesh.tris[i] = applyTransformMatrixToTria(mesh.tris[i],midCorrection)
            mesh.tris[i] = applyTransformMatrixToTria(mesh.tris[i],matZRot)
            mesh.tris[i] = applyTransformMatrixToTria(mesh.tris[i],matYRot)
            mesh.tris[i] = applyTransformMatrixToTria(mesh.tris[i],matXRot)
            newTriaRotatedX = applyTransformMatrixToTria(mesh.tris[i],identityMatrix)
            //newMiddled = applyTransformMatrixToTria(mesh.tris[i],midCorrection)
//
            //newTriaRotatedY = applyTransformMatrixToTria(newMiddled,matYRot)
//
            //newTriaRotatedX = applyTransformMatrixToTria(newTriaRotatedY,matXRot)

            newTriaRotatedX.p[0].z += 2.5f
            newTriaRotatedX.p[1].z += 2.5f
            newTriaRotatedX.p[2].z += 2.5f

            line1 = vec3d(newTriaRotatedX.p[1].x - newTriaRotatedX.p[0].x
                ,newTriaRotatedX.p[1].y - newTriaRotatedX.p[0].y
                ,newTriaRotatedX.p[1].z - newTriaRotatedX.p[0].z)
            line2 = vec3d(newTriaRotatedX.p[2].x - newTriaRotatedX.p[0].x
                ,newTriaRotatedX.p[2].y - newTriaRotatedX.p[0].y
                ,newTriaRotatedX.p[2].z - newTriaRotatedX.p[0].z)
            normal = vec3d(line1.y * line2.z - line1.z * line2.y
                ,line1.z * line2.x - line1.x * line2.z
                ,line1.x * line2.y - line1.y * line2.x)
            normilazeVec(normal, calculateLengthOfVec(normal))
            if (scalarProduct(normal,vecDiff(newTriaRotatedX.p[0],vCamera)) < 0) {
                newTriaInCameraFOV = applyTransformMatrixToTria(newTriaRotatedX,matProj)

                newTriaInCameraFOV.t[0].x = newTriaInCameraFOV.t[0].x / newTriaInCameraFOV.p[0].w
                newTriaInCameraFOV.t[1].x = newTriaInCameraFOV.t[1].x / newTriaInCameraFOV.p[1].w
                newTriaInCameraFOV.t[2].x = newTriaInCameraFOV.t[2].x / newTriaInCameraFOV.p[2].w

                newTriaInCameraFOV.t[0].y = newTriaInCameraFOV.t[0].y / newTriaInCameraFOV.p[0].w
                newTriaInCameraFOV.t[1].y = newTriaInCameraFOV.t[1].y / newTriaInCameraFOV.p[1].w
                newTriaInCameraFOV.t[2].y = newTriaInCameraFOV.t[2].y / newTriaInCameraFOV.p[2].w

                newTriaInCameraFOV.t[0].w = 1.0f / newTriaInCameraFOV.p[0].w
                newTriaInCameraFOV.t[1].w = 1.0f / newTriaInCameraFOV.p[1].w
                newTriaInCameraFOV.t[2].w = 1.0f / newTriaInCameraFOV.p[2].w

                newTriaInCameraFOV.p[0] = vecDiv(newTriaInCameraFOV.p[0],newTriaInCameraFOV.p[0].w)
                newTriaInCameraFOV.p[1] = vecDiv(newTriaInCameraFOV.p[1],newTriaInCameraFOV.p[1].w)
                newTriaInCameraFOV.p[2] = vecDiv(newTriaInCameraFOV.p[2],newTriaInCameraFOV.p[2].w)

                newTriaInCameraFOV.p[0].x += 1.0f; newTriaInCameraFOV.p[0].y += 1.0f
                newTriaInCameraFOV.p[1].x += 1.0f; newTriaInCameraFOV.p[1].y += 1.0f
                newTriaInCameraFOV.p[2].x += 1.0f; newTriaInCameraFOV.p[2].y += 1.0f

                newTriaInCameraFOV.p[0].x *= 0.5f * image.width; newTriaInCameraFOV.p[0].y *= 0.5f * image.height
                newTriaInCameraFOV.p[1].x *= 0.5f * image.width; newTriaInCameraFOV.p[1].y *= 0.5f * image.height
                newTriaInCameraFOV.p[2].x *= 0.5f * image.width; newTriaInCameraFOV.p[2].y *= 0.5f * image.height


                drawTriangle(pixels,newTriaInCameraFOV,image.width,image.height,texture,texWidth)
            }
        }
        image.setPixels(pixels, 0, image.width, 0, 0, image.width, image.height)

    }
    fun applyTransformMatrixToTria(oldTria:Tria3d,matrix:mat4x4):Tria3d{
        val newTriaTransformed = Tria3d(
            vec3d(0.0f,0.0f,0.0f),
            vec3d(0.0f,0.0f,0.0f),
            vec3d(0.0f,0.0f,0.0f),
            vec2d(oldTria.t[0].x,oldTria.t[0].y),
            vec2d(oldTria.t[1].x,oldTria.t[1].y),
            vec2d(oldTria.t[2].x,oldTria.t[2].y)
        )
        multiply4x4MatVec(oldTria.p[0],newTriaTransformed.p[0],matrix)
        multiply4x4MatVec(oldTria.p[1],newTriaTransformed.p[1],matrix)
        multiply4x4MatVec(oldTria.p[2],newTriaTransformed.p[2],matrix)
        return newTriaTransformed
    }
}