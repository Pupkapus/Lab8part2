package com.example.lab8

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Car(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var Mark: String = "",
                 var Company: String = "",
                 var Price: Int = 0)