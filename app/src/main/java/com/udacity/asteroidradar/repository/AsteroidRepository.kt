package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.Asteroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidsDatabase) {

    var asteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids()) { it.asDomainModel() }

    val todayAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getTodayAsteroids(
        START_DATE)) {it.asDomainModel()}

    val weekAsteroid: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getWeekAsteroids(
        START_DATE, END_DATE)) {it.asDomainModel()}

    suspend fun refreshAsteroid() {
        withContext(Dispatchers.IO){
            val content = withContext(Dispatchers.Default) { NasaApi.retrofitService.getAsteroids(START_DATE, END_DATE, Constants.API_KEY) }
            val obj = JSONObject(content)
            val netAsteroid = NetworkAsteroidContainer(parseAsteroidsJsonResult(obj))
            database.asteroidDao.insertAll(*netAsteroid.asDatabaseModel())
        }
    }

    suspend fun refreshTodayAsteroids() {
        withContext(Dispatchers.IO){
            val content = withContext(Dispatchers.Default) { NasaApi.retrofitService.getAsteroids(START_DATE, START_DATE, Constants.API_KEY) }
            val obj = JSONObject(content)
            val netAsteroid = NetworkAsteroidContainer(parseAsteroidsJsonResult(obj))
            database.asteroidDao.insertAll(*netAsteroid.asDatabaseModel())
        }
    }

    suspend fun deleteYesterdayAsteroids() {
        withContext(Dispatchers.IO) {
            database.asteroidDao.deleteYesterday(getYesterday() )
        }
    }

    private fun getYesterday() : String {
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }
}