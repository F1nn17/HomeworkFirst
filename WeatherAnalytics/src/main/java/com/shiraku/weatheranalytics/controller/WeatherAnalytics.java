package com.shiraku.weatheranalytics.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.shiraku.weatheranalytics.entity.WeatherRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class WeatherAnalytics {
    private final Set<String> targetCities = Set.of("Магадан", "Чукотка", "Санкт-Петербург", "Тюмень");
    private final Map<String, List<WeatherRecord>> cityWeatherData = new HashMap<>();

    @KafkaListener(
            topics = "weather_topic",
            groupId = "weather-analytics-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(JsonNode data) {
        String city = data.path("city").asText();
        if (!targetCities.contains(city)) {
            return;
        }
        LocalDateTime date = parseDate(data.path("date"));
        int temperature = data.path("temperature").asInt(-999);
        String condition = data.path("condition").asText("unknown");
        WeatherRecord record = new WeatherRecord(date, temperature, condition);
        cityWeatherData.computeIfAbsent(city, k -> new ArrayList<>()).add(record);
    }

    @Scheduled(fixedDelay = 210*1000)
    public void scheduledAnalyticsCheck(){
        runWeeklyAnalytics();
        resetData();
    }

    public void runWeeklyAnalytics() {
        System.out.println("\n\n--- Еженедельная аналитика погоды ---\n");

        // Самое большое количество дождливых дней
        Map<String, Integer> rainyDaysCount = new HashMap<>();
        // Самая высокая температура
        Map<String, Integer> maxTempMap = new HashMap<>();
        AtomicReference<String> maxCity = new AtomicReference<>("");
        AtomicInteger maxTemp = new AtomicInteger(Integer.MIN_VALUE);
        // Город с самой низкой средней температурой
        Map<String, Double> avgTempMap = new HashMap<>();

        for (Map.Entry<String, List<WeatherRecord>> entry : cityWeatherData.entrySet()) {
            String city = entry.getKey();
            List<WeatherRecord> records = entry.getValue();

            // Считаем дождливые дни
            long rainyDays = records.stream()
                    .filter(r -> r.condition().toLowerCase().contains("rain") || r.condition().toLowerCase().contains("дождь"))
                    .count();
            rainyDaysCount.put(city, (int) rainyDays);

            // Находим максимальную температуру
            Optional<Integer> maxTempOpt = records.stream()
                    .map(WeatherRecord::temperature)
                    .max(Integer::compareTo);
            maxTempOpt.ifPresent(temp -> {
                if (temp > maxTemp.get()) {
                    maxTemp.set(temp);
                    maxCity.set(city);
                }
            });

            // Средняя температура
            double avgTemp = records.stream()
                    .mapToInt(WeatherRecord::temperature)
                    .average()
                    .orElse(Double.NaN);
            avgTempMap.put(city, avgTemp);
        }

        // 1. Самое большое количество дождливых дней
        Optional<Map.Entry<String, Integer>> rainiest = rainyDaysCount.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());

        rainiest.ifPresent(entry ->
                System.out.printf("Самое большое количество дождливых дней: %d — в городе %s%n",
                        entry.getValue(), entry.getKey()));

        // 2. Самая жаркая погода
        if (!maxCity.get().isEmpty()) {
            System.out.printf("Самая жаркая погода была %s°C в городе %s%n",
                    maxTemp, maxCity);
        }

        // 3. Самая низкая средняя температура
        avgTempMap.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .ifPresent(entry ->
                        System.out.printf("Самая низкая средняя температура %.1f°C в городе %s%n",
                                entry.getValue(), entry.getKey()));
    }

    private void resetData() {
        cityWeatherData.clear();
        System.out.println("Статистика за неделю сброшена.");
    }

    private LocalDateTime parseDate(JsonNode dateNode) {
        if (dateNode.isArray() && dateNode.size() >= 6) {
            int year = dateNode.get(0).asInt();
            int month = dateNode.get(1).asInt();
            int day = dateNode.get(2).asInt();
            int hour = dateNode.get(3).asInt();
            int minute = dateNode.get(4).asInt();
            int second = dateNode.get(5).asInt();

            return LocalDateTime.of(year, month, day, hour, minute, second);
        }
        return LocalDateTime.now();
    }
}
