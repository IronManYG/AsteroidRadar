package com.udacity.asteroidradar.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.END_DATE
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.START_DATE
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class NasaApiStatus { LOADING, ERROR, DONE }
/**
 * The [ViewModel] that is attached to the [MainViewModel].
 */
class MainViewModel : ViewModel() {
    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<NasaApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<NasaApiStatus>
        get() = _status

    // Internally, we use a MutableLiveData, because we will be updating the List of Asteroid
    // with new values
    private val _asteroids = MutableLiveData<List<Asteroid>>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids

    // Internally, we use a MutableLiveData to handle navigation to the selected property
    private val _navigateToSelectedAsteroids = MutableLiveData<Asteroid>()

    // The external immutable LiveData for the navigation property
    val navigateToSelectedProperty: LiveData<Asteroid>
        get() = _navigateToSelectedAsteroids

    /**
     * Call getAsteroid() on init so we can display status immediately.
     */
    init {
        getAsteroid()
    }

    /**
     * Gets filtered Asteroid information from the Nasa API Retrofit service and
     * updates the [Asteroid] [List] and [NasaApiStatus] [LiveData]. The Retrofit service
     * returns a coroutine Deferred, which we await to get the result of the transaction.
     * @param filter the [MarsApiFilter] that is sent as part of the web server request
     */
    private fun getAsteroid() {
        viewModelScope.launch {
            _status.value = NasaApiStatus.LOADING
            try {
                val content = async{NasaApi.retrofitService.getAsteroids(START_DATE,END_DATE,Constants.API_KEY)}.await()
                Log.v("MainViewModel", content)
                val obj = JSONObject(content)
                _asteroids.value = parseAsteroidsJsonResult(obj).toMutableList()
                _status.value = NasaApiStatus.DONE
            } catch (e: Exception) {
                _status.value = NasaApiStatus.ERROR
                _asteroids.value = ArrayList()
            }
        }
    }


    /**
     * When the asteroid is clicked, set the [_navigateToSelectedAsteroids] [MutableLiveData]
     * @param asteroid The [Asteroid] that was clicked on.
     */
    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroids.value = asteroid
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedProperty is set to null
     */
    fun displayPropertyDetailsComplete() {
        _navigateToSelectedAsteroids.value = null
    }

    /**
     * Updates the data set filter for the web services by querying the data with the new filter
     * by calling [getMarsRealEstateProperties]
     * @param filter the [MarsApiFilter] that is sent as part of the web server request
     */
    fun updateFilter() {
        getAsteroid()
    }
}