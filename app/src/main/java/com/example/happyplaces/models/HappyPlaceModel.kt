package com.example.happyplaces.models

 data class HappyPlaceModel(
     val id: Int,
     val title: String,
     val image: String, // Path of the image
     val description: String,
     val date: String,
     val location: String,
     val latitude: Double,
     val longitude: Double
 )