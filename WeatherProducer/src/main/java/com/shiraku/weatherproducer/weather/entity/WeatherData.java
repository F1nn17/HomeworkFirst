package com.shiraku.weatherproducer.weather.entity;

import java.time.LocalDateTime;

public record WeatherData(String city, LocalDateTime date, int temperature, String condition) {
}
