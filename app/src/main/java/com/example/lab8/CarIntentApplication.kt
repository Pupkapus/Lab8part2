package com.example.lab8

import android.app.Application
import com.example.lab8.CarRepository

class CarIntentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CarRepository.initialize(this)
    }
}