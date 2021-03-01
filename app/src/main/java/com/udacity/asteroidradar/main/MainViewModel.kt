package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.START_DATE
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

enum class NasaApiStatus { LOADING, ERROR, DONE }
enum class NasaApiFilter { SHOW_TODAY, SHOW_WEEK, SHOW_ALL}

/**
 * The [ViewModel] that is attached to the [MainViewModel].
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<NasaApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<NasaApiStatus>
        get() = _status

    // Image of the day data
    private val _dailyPicture = MutableLiveData<PictureOfDay>()
    val dailyPicture: LiveData<PictureOfDay>
        get() = _dailyPicture

    // Internally, we use a MutableLiveData to handle navigation to the selected property
    private val _navigateToSelectedAsteroids = MutableLiveData<Asteroid>()

    // The external immutable LiveData for the navigation property
    val navigateToSelectedProperty: LiveData<Asteroid>
        get() = _navigateToSelectedAsteroids

    // set filter for asteroid request
    private val nasaApiFilter = MutableLiveData<NasaApiFilter>(NasaApiFilter.SHOW_WEEK)

    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    /**
     * Call getAsteroid() on init so we can display status immediately.
     */
    init {
        viewModelScope.launch {
            refreshImage()
            refreshAsteroid()
        }
    }

    val asteroids = Transformations.switchMap(nasaApiFilter){
        when(it){
            NasaApiFilter.SHOW_TODAY -> asteroidRepository.todayAsteroids
            NasaApiFilter.SHOW_WEEK -> asteroidRepository.weekAsteroid
            else -> asteroidRepository.asteroids
        }
    }

    private suspend fun refreshImage() {
        _status.value = NasaApiStatus.LOADING
        try {
            _dailyPicture.value = NasaApi.retrofitService.getImageOfTheDay(Constants.API_KEY)
            _status.value = NasaApiStatus.DONE
        } catch (e: Exception) {
            e.printStackTrace()
            _status.value = NasaApiStatus.ERROR
        }
    }

    private suspend fun refreshAsteroid() {
        _status.value = NasaApiStatus.LOADING
        try {
            asteroidRepository.refreshAsteroid()
            _status.value = NasaApiStatus.DONE
        } catch (e: Exception){
            e.printStackTrace()
            _status.value = NasaApiStatus.ERROR
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
     * After the navigation has taken place, make sure navigateToSelectedAsteroids is set to null
     */
    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroids.value = null
    }

    /**
     * Updates the data set filter for the web services by querying the data with the new filter
     * by calling []
     * @param filter the [NasaApiFilter] that is sent as part of the web server request
     */
    fun updateFilter(filter: NasaApiFilter) {
        nasaApiFilter.value = filter
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}