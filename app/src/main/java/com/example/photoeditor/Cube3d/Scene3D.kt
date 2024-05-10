package com.example.photoeditor.Cube3d

import android.graphics.Bitmap
import com.example.photoeditor.UsefulFuns.calculateLengthOfVec
import com.example.photoeditor.UsefulFuns.normilazeVec
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Scene3D {
    var mesh:Mesh = Mesh()
    val matProj = mat4x4()
    val matYRot = mat4x4()
    val matXRot = mat4x4()
    val matZRot = mat4x4()
    val midCorrection = mat4x4()
    lateinit var vCamera:vec3d
    fun createCamera(fNear:Float, fFar:Float,fFov:Float,fAspectRatio:Float,pos:vec3d)
    {
        val fFovRad = 1.0f / tan(fFov * 0.5f/180.0f * 3.14159f)
        vCamera = pos

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
        mesh.tris = obj
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
    fun drawMesh(image: Bitmap){

        val pixels = IntArray(image.width*image.height)
        image.getPixels(pixels,0,image.width,0,0,image.width,image.height)

        for (i in 0..<mesh.tris.size){

            val newMiddled = applyTransformMatrixToTria(mesh.tris[i],midCorrection)

            val newTriaRotatedY = applyTransformMatrixToTria(newMiddled,matYRot)

            val newTriaRotatedX = applyTransformMatrixToTria(newTriaRotatedY,matXRot)

            newTriaRotatedX.p[0].z = newTriaRotatedX.p[0].z + 3.0f
            newTriaRotatedX.p[1].z = newTriaRotatedX.p[1].z + 3.0f
            newTriaRotatedX.p[2].z = newTriaRotatedX.p[2].z + 3.0f

            val line1 = vec3d(arrayOf(newTriaRotatedX.p[1].x - newTriaRotatedX.p[0].x
                ,newTriaRotatedX.p[1].y - newTriaRotatedX.p[0].y
                ,newTriaRotatedX.p[1].z - newTriaRotatedX.p[0].z))
            val line2 = vec3d(arrayOf(newTriaRotatedX.p[2].x - newTriaRotatedX.p[0].x
                ,newTriaRotatedX.p[2].y - newTriaRotatedX.p[0].y
                ,newTriaRotatedX.p[2].z - newTriaRotatedX.p[0].z))
            val normal = vec3d(arrayOf(line1.y * line2.z - line1.z * line2.y
                ,line1.z * line2.x - line1.x * line2.z
                ,line1.x * line2.y - line1.y * line2.x))
            normilazeVec(normal, calculateLengthOfVec(normal))
            if (normal.x * (newTriaRotatedX.p[0].x - vCamera.x) +
                normal.y * (newTriaRotatedX.p[0].y - vCamera.y) +
                normal.z * (newTriaRotatedX.p[0].z - vCamera.z) >= 0) continue
            val newTriaInCameraFOV = applyTransformMatrixToTria(newTriaRotatedX,matProj)

            newTriaInCameraFOV.p[0].x += 1.0f; newTriaInCameraFOV.p[0].y += 1.0f
            newTriaInCameraFOV.p[1].x += 1.0f; newTriaInCameraFOV.p[1].y += 1.0f
            newTriaInCameraFOV.p[2].x += 1.0f; newTriaInCameraFOV.p[2].y += 1.0f

            newTriaInCameraFOV.p[0].x *= 0.5f * image.width; newTriaInCameraFOV.p[0].y *= 0.5f * image.height
            newTriaInCameraFOV.p[1].x *= 0.5f * image.width; newTriaInCameraFOV.p[1].y *= 0.5f * image.height
            newTriaInCameraFOV.p[2].x *= 0.5f * image.width; newTriaInCameraFOV.p[2].y *= 0.5f * image.height

            drawTriangle(pixels,newTriaInCameraFOV,image.width,image.height)

        }
        image.setPixels(pixels, 0, image.width, 0, 0, image.width, image.height)

    }
}