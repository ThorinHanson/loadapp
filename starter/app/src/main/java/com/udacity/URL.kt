package com.udacity

enum class URL(val path: String, val title: String, val message: String) {
    GLIDE(
        "https://github.com/bumptech/glide/archive/master.zip",
        "Glide: Image Loading Library By BumpTech",
        "Glide repository is downloaded"
    ),
    LOADAPP(
        "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip",
        "Udacity: Android Kotlin Nanodegree",
        "The Project 3 repository is downloaded"),
    RETROFIT(
        "https://github.com/square/retrofit/archive/master.zip",
        "Retrofit: Type-safe HTTP client by Square, Inc",
        "Retrofit repository is downloaded")
}