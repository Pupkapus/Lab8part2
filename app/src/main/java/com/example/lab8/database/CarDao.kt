package com.example.lab8.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.lab8.Car
import java.util.*

@Dao
interface CarDao {

    @Query("SELECT * FROM car")
    fun getCars(): LiveData<List<Car>>

    @Query("SELECT * FROM car WHERE id=(:id)")
    fun getCar(id: UUID): LiveData<Car?>

    @Update
    fun updateCar(car: Car)

    @Insert
    fun addCar(car: Car)
}