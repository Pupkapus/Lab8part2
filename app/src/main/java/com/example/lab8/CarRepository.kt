package com.example.lab8

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.lab8.database.CarDatabase
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "car-database"

class CarRepository private constructor(context: Context) {

    private val database : CarDatabase = Room.databaseBuilder(
        context.applicationContext,
        CarDatabase::class.java,
        DATABASE_NAME
    ).build()
    private val carDao = database.carDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getCars(): LiveData<List<Car>> = carDao.getCars()

    fun getCar(id: UUID): LiveData<Car?> = carDao.getCar(id)

    fun updateCar(car: Car) {
        executor.execute {
            carDao.updateCar(car)
        }
    }

    fun addCar(car: Car) {
        executor.execute {
            carDao.addCar(car)
        }
    }

    companion object {
        private var INSTANCE: CarRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CarRepository(context)
            }
        }

        fun get(): CarRepository {
            return INSTANCE ?:
            throw IllegalStateException("CarRepository must be initialized")
        }
    }
}