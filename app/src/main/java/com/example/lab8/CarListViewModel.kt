package com.example.lab8

import androidx.lifecycle.ViewModel

class CarListViewModel : ViewModel() {

    private val carRepository = CarRepository.get()
    val carListLiveData = carRepository.getCars()

    fun addCar(car: Car) {
        carRepository.addCar(car)
    }
}