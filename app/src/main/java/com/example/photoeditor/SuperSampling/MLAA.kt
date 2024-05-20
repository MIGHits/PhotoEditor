package com.example.photoeditor.SuperSampling

import android.graphics.Bitmap
import android.graphics.Color.alpha
import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import androidx.core.graphics.set
import com.example.photoeditor.UsefulFuns.createColor
import kotlin.math.abs

class Point(){
    // 1 == справа 0 == снизу
    var smoothMode:Int? = null
    var curIndex:Int? = null
}
class groupToSmooth(){
    var startX:Int? = null
    var startY:Int? = null
    var end:Int? = null
    var left:Boolean = false
    var right:Boolean = false
    var top:Boolean = false
    var bot:Boolean = false
}
fun MLAA(sourceImage:Bitmap):Bitmap{
    val points = Array(sourceImage.height){Array(sourceImage.width) {Point()} }
    val groups = ArrayList<groupToSmooth>()
    var changedBitmap = Bitmap.createBitmap(sourceImage.width, sourceImage.height, Bitmap.Config.ARGB_8888)

    var absIndex = -1

    for(y in 1..sourceImage.height-2){
        for(x in 1..sourceImage.width-2){
            changedBitmap.setPixel(x,y,sourceImage.getPixel(x,y))
            val isBot = comparePixel(sourceImage.getPixel(x,y),sourceImage.getPixel(x,y+1))
            val isLeft = comparePixel(sourceImage.getPixel(x-1,y),sourceImage.getPixel(x,y))
            val isRight = comparePixel(sourceImage.getPixel(x+1,y),sourceImage.getPixel(x,y))
            val isTop = comparePixel(sourceImage.getPixel(x,y),sourceImage.getPixel(x,y-1))
            val wasLeft = points[y][x-1].smoothMode == 0
            val wasTop = points[y-1][x].smoothMode == 1


            if (isBot){
                if(wasLeft){
                    points[y][x].curIndex = points[y][x-1].curIndex
                    points[y][x].smoothMode = points[y][x-1].smoothMode
                    groups[points[y][x].curIndex!!].end = x
                    if(isRight){
                        groups[points[y][x].curIndex!!].left = true
                    }
                }else{
                    points[y][x].curIndex = absIndex+1
                    absIndex++
                    points[y][x].smoothMode = 0
                    groups.add(groupToSmooth())
                    groups[points[y][x].curIndex!!].startX = x
                    groups[points[y][x].curIndex!!].startY = y
                    if(isLeft){
                        groups[points[y][x].curIndex!!].right = true
                    }
                }

            }
            if (isRight){
                if(wasTop){
                    points[y][x].curIndex = points[y-1][x].curIndex
                    points[y][x].smoothMode = points[y-1][x].smoothMode
                    groups[points[y][x].curIndex!!].end = y
                    if(isBot){
                        groups[points[y][x].curIndex!!].top = true
                    }
                }else{
                    points[y][x].curIndex = absIndex+1
                    absIndex++
                    points[y][x].smoothMode = 1
                    groups.add(groupToSmooth())
                    groups[points[y][x].curIndex!!].startX = x
                    groups[points[y][x].curIndex!!].startY = y
                    if(isTop){
                        groups[points[y][x].curIndex!!].bot = true
                    }

                }
            }
        }

    }
    for(i in 0..<groups.count()){
        val curGroup = groups[i]
        if (curGroup.end == null){
            continue
        }

        if (points[curGroup.startY!!][curGroup.startX!!].smoothMode == 0){
            var len = (-curGroup.startX!! + curGroup.end!!).toFloat()
            if(curGroup.left && !curGroup.right){
                for(x in curGroup.startX!!..curGroup.end!!){
                    val curPixel = changedBitmap.getPixel(x,curGroup.startY!!)
                    val botPixel = changedBitmap.getPixel(x,curGroup.startY!!+1)
                    val newPixel = interpolation(curPixel,botPixel,(x-curGroup.startX!!+1)/(len+1))
                    changedBitmap.setPixel(x,curGroup.startY!!, newPixel)
                }
            }
            if(curGroup.right && !curGroup.left){
                for(x in curGroup.startX!!..curGroup.end!!){
                    val curPixel = changedBitmap.getPixel(x,curGroup.startY!!)
                    val botPixel = changedBitmap.getPixel(x,curGroup.startY!!+1)
                    val newPixel = interpolation(curPixel,botPixel,(abs(curGroup.end!!-x+1))/(len+1))
                    changedBitmap.setPixel(x,curGroup.startY!!, newPixel)
                }
            }
            if(curGroup.right && curGroup.left){
                len/=2
                for(x in curGroup.startX!!..curGroup.end!!){
                    val curPixel = changedBitmap.getPixel(x,curGroup.startY!!)
                    val botPixel = changedBitmap.getPixel(x,curGroup.startY!!+1)
                    val newPixel = interpolation(curPixel,botPixel,(abs((curGroup.startX!!+len)-x+1))/(len+1))
                    changedBitmap.setPixel(x,curGroup.startY!!, newPixel)
                }
            }
        }else{
            var len = (-curGroup.startY!! + curGroup.end!!).toFloat()
            if(curGroup.top && !curGroup.bot){
                for(y in curGroup.startY!!..curGroup.end!!){
                    val curPixel = changedBitmap.getPixel(curGroup.startX!!,y)
                    val rightPixel = changedBitmap.getPixel(curGroup.startX!!+1,y)
                    val newPixel = interpolation(curPixel,rightPixel,(y-curGroup.startY!!+1)/(len+1))
                    changedBitmap.setPixel(curGroup.startX!!,y, newPixel)
                }
            }
            if(curGroup.bot && !curGroup.top){
                for(y in curGroup.startY!!..curGroup.end!!){
                    val curPixel = changedBitmap.getPixel(curGroup.startX!!,y)
                    val rightPixel = changedBitmap.getPixel(curGroup.startX!!+1,y)
                    val newPixel = interpolation(curPixel,rightPixel,(abs(curGroup.end!!-y+1))/(len+1))
                    changedBitmap.setPixel(curGroup.startX!!,y, newPixel)
                }
            }
            if(curGroup.top && curGroup.bot){
                len/=2
                for(y in curGroup.startY!!..curGroup.end!!){
                    val curPixel = changedBitmap.getPixel(curGroup.startX!!,y)
                    val rightPixel = changedBitmap.getPixel(curGroup.startX!!+1,y)
                    val newPixel = interpolation(curPixel,rightPixel,(abs((curGroup.startY!!+len)-y+1))/(len+1))
                    changedBitmap.setPixel(curGroup.startX!!,y, newPixel)
                }
            }
        }
    }
    return changedBitmap
}
fun interpolation(fcolor:Int,scolor:Int,coef:Float):Int{
    val newRed = red(fcolor) + (red(scolor) - red(fcolor))*coef
    val newGreen = green(fcolor) + (green(scolor) - green(fcolor))*coef
    val newBlue = blue(fcolor) + (blue(scolor) - blue(fcolor))*coef
    return createColor(newRed.toInt(),newGreen.toInt(),newBlue.toInt(),alpha(fcolor))
}
fun comparePixel(p1:Int,p2:Int):Boolean {
    val cmprFac = 255*0.2f
    if ((red(p1) >= red(p2)-cmprFac && red(p1) <= red(p2)+cmprFac)
        && (blue(p1) >= blue(p2)-cmprFac && blue(p1) <= blue(p2)+cmprFac)
        && (green(p1) >= green(p2)-cmprFac && green(p1) <= green(p2)+cmprFac)){
        return false
    }
    return true
}