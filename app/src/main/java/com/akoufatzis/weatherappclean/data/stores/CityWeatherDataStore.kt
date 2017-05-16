package com.akoufatzis.weatherappclean.data.stores

import com.akoufatzis.weatherappclean.BuildConfig
import com.akoufatzis.weatherappclean.data.api.RestApi
import com.akoufatzis.weatherappclean.data.cache.MemoryCache
import com.akoufatzis.weatherappclean.data.entities.CityWeatherEntity
import com.akoufatzis.weatherappclean.data.mappers.mapToCityWeather
import com.akoufatzis.weatherappclean.domain.models.CityWeather
import com.akoufatzis.weatherappclean.domain.models.Result
import com.akoufatzis.weatherappclean.domain.repositories.WeatherRepository
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by alexk on 05.05.17.
 */
@Singleton
class CityWeatherDataStore @Inject constructor(val restApi: RestApi) : WeatherRepository {

    // TODO: inject this
    private val memoryCache = MemoryCache<CityWeatherEntity>()
    private val apiKey = BuildConfig.OPENWEATHERMAP_API_KEY

    override fun loadCityWeatherData(searchTerm: String): Observable<Result<CityWeather>> {

        return Observable.concat(loadFromCache(searchTerm), loadFromDb(searchTerm), loadFromNetwork(searchTerm))
                .firstElement()
                .toObservable()
                .doOnNext {
                    if (it.data != null) {
                        saveToCache(searchTerm, it.data)
                    }
                }
                .compose(mapToCityWeather())
                .onErrorReturn { Result.error(it) }
    }

    private fun loadFromNetwork(searchTerm: String): Observable<Result<CityWeatherEntity>> {
        return restApi.getWeatherByCityName(searchTerm, apiKey)
                .flatMap {
                    if (!it.isSuccessful) {
                        Observable.just(Result.error(Throwable(it.errorBody().string())))
                    } else {
                        Observable.just(Result.success(it.body()))
                    }
                }
    }

    private fun loadFromCache(searchTerm: String): Observable<Result<CityWeatherEntity>> {
        return memoryCache[searchTerm].map { Result.success(it) }
    }

    private fun loadFromDb(searchTerm: String) = Observable.empty<Result<CityWeatherEntity>>()

    private fun saveToCache(key: String, entity: CityWeatherEntity) {
        memoryCache.put(key, entity, MemoryCache.VALIDATION_PERIOD)
    }
}