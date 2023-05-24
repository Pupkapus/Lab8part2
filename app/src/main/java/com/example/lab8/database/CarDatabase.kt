package com.example.lab8.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lab8.Car

@Database(entities = [ Car::class ], version=1)
@TypeConverters(CarTypeConverters::class)
abstract class CarDatabase : RoomDatabase() {

    abstract fun carDao(): CarDao
}