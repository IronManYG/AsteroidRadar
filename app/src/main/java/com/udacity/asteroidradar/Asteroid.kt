package com.udacity.asteroidradar

import android.os.Parcelable
import androidx.lifecycle.LiveData
import kotlinx.android.parcel.Parcelize

/**
 * Gets Asteroid information from the Nas API Retrofit service and updates the
 * [Asteroid] and [NasaApiStatus] [LiveData]. The Retrofit service returns a coroutine
 * Deferred, which we await to get the result of the transaction.
 * @param filter the [NasaApiStatus] that is sent as part of the web server request
 */
@Parcelize
data class Asteroid(val id: Long,val codename: String, val closeApproachDate: String,
                    val absoluteMagnitude: Double, val estimatedDiameter: Double,
                    val relativeVelocity: Double, val distanceFromEarth: Double,
                    val isPotentiallyHazardous: Boolean) : Parcelable