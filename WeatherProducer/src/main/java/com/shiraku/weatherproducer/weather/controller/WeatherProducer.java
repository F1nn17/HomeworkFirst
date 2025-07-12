package com.shiraku.weatherproducer.weather.controller;

import com.shiraku.weatherproducer.weather.entity.City;
import com.shiraku.weatherproducer.weather.entity.WeatherData;
import com.shiraku.weatherproducer.weather.service.WeatherService;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.kafka.common.requests.FetchMetadata.log;

@Controller
public class WeatherProducer {
    private final KafkaTemplate<String, WeatherData> kafkaTemplate;
    private final WeatherService weatherService;
    private final NewTopic weatherTopic;
    private Map<City, List<WeatherData>> forecastPerCity;
    private int dayIndex = 0;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public WeatherProducer(KafkaTemplate<String, WeatherData> kafkaTemplate, WeatherService weatherService, NewTopic weatherTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.weatherService = weatherService;
        this.weatherTopic = weatherTopic;
        this.forecastPerCity = weatherService.generateWeeklyForecast();
    }

    @Scheduled(fixedDelay = 30*1000)
    public void sendWeatherPeriodically() {
        if (!isRunning.compareAndSet(false, true)) {
            System.out.println("Previous execution is still running, skipping...");
            return;
        }

        try {
            System.out.println("Sending weather data day at " + dayIndex);
            forecastPerCity.keySet().forEach(city -> {
                WeatherData data = forecastPerCity.get(city).get(dayIndex);
                kafkaTemplate.send(weatherTopic.name(), data);
            });

            dayIndex++;
            if (dayIndex >= 7) {
                forecastPerCity = weatherService.generateWeeklyForecast();
                dayIndex = 0;
            }
        }
        catch (Exception e) {
            log.error("Failed to send weather data", e);
        }
        finally {
            isRunning.set(false);
        }
    }

}
