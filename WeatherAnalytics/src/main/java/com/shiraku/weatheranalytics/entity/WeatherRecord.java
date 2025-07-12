package com.shiraku.weatheranalytics.entity;

import java.time.LocalDateTime;

public record WeatherRecord(LocalDateTime date, int temperature, String condition) {
}
