package com.example.photoeditor.Cube3d

class vec3d {
    var x:Float
    var y:Float
    var z:Float
    var w:Float = 0.0f
    constructor(x:Float,y:Float,z:Float,w:Float){
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }
    constructor(x:Float,y:Float,z:Float){
        this.x = x
        this.y = y
        this.z = z
    }
}