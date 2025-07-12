package com.shiraku.weatherproducer.weather.service;

import com.shiraku.weatherproducer.weather.entity.City;
import com.shiraku.weatherproducer.weather.entity.WeatherCondition;
import com.shiraku.weatherproducer.weather.entity.WeatherData;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class WeatherService {
    private static final Random random = new Random();

    public WeatherData generateWeatherData(City city) {
        LocalDateTime localDate = LocalDateTime.now();
        int temperature = random.nextInt(36);
        WeatherCondition condition = WeatherCondition.getRandomWeatherCondition();
        return new WeatherData(city.getDisplayName(), localDate,temperature, condition.getDisplayName());
    }

    public Map<City, List<WeatherData>> generateWeeklyForecast() {
        Map<City, List<WeatherData>> forecast = new HashMap<>();
        for (City city : City.values()) {
            List<WeatherData> weekForecast = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                weekForecast.add(generateWeatherData(city));
            }
            forecast.put(city, weekForecast);
        }
        return forecast;
    }
}
