package com.example.photoeditor

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.MainActivity

class SplashScreen : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            // После задержки переходим на MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            // Закрываем эту активность
            finish()
        }, SPLASH_TIME_OUT)
    }
}