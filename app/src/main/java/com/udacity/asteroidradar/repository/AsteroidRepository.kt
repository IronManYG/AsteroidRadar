package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.END_DATE
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.START_DATE
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AsteroidRepository(private val database: AsteroidsDatabase) {

    val asteroids: LiveData<List<Asteroid>> =
            Transformations.map(database.asteroidDao.getWeekAsteroids()) {
                it.asDomainModel()
            }

    suspend fun refreshAsteroid() {
        withContext(Dispatchers.IO){
            val content = withContext(Dispatchers.Default) { NasaApi.retrofitService.getAsteroids(START_DATE, END_DATE, Constants.API_KEY) }
            val obj = JSONObject(content)
            val asteroid = parseAsteroidsJsonResult(obj).toMutableList()
        }
    }
}