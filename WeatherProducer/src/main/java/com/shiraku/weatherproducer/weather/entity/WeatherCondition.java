package com.shiraku.weatherproducer.weather.entity;

import lombok.Getter;

@Getter
public enum WeatherCondition {
    SUNNY("Солнечно"),
    CLOUDY("Облачно"),
    RAINY("Дождь");

    private final String displayName;

    WeatherCondition(String displayName) {
        this.displayName = displayName;
    }

    public static WeatherCondition getRandomWeatherCondition() {
        WeatherCondition[] values = WeatherCondition.values();
        int randomIndex = (int) (Math.random() * values.length);
        return values[randomIndex];
    }
}
