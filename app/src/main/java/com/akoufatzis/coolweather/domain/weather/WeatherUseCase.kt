package com.akoufatzis.coolweather.domain.weather

import com.akoufatzis.coolweather.domain.Result
import javax.inject.Inject

class WeatherUseCase @Inject constructor(private val weatherRepository: WeatherRepository) {

    suspend operator fun invoke(city: String): Result<CityWeather> {
        return weatherRepository.fetchCityWeatherData(city)
    }
}
