package com.example.lab8

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class CarDetailViewModel : ViewModel() {

    private val carRepository = CarRepository.get()
    private val carIdLiveData = MutableLiveData<UUID>()

    val carLiveData: LiveData<Car?> =
        Transformations.switchMap(carIdLiveData) { carId ->
            carRepository.getCar(carId)
        }

    fun loadCar(carId: UUID) {
        carIdLiveData.value = carId
    }

    fun saveCar(car: Car) {
        carRepository.updateCar(car)
    }
}