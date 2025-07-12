package com.shiraku.weatherconsumer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.shiraku.weatherconsumer.util.DateFormatter;
import de.vandermeer.asciitable.AT_Cell;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WeatherConsumer {
    private final Map<String, TreeMap<String, String>> cityWeatherData = new HashMap<>();

    private int messageCount = 0;

    private boolean tablePrinted = false;

    @KafkaListener(
            topics = "weather_topic",
            groupId = "weather-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(JsonNode data) {
        String city = data.path("city").asText("N/A");
        String date = "N/A";
        if (data.path("date").isArray()) {
            int[] timestamp = new int[7];
            for (int i = 0; i < 7; i++) {
                timestamp[i] = data.get("date").get(i).asInt();
            }
            date = DateFormatter.formatTimestamp(timestamp);
        }
        int temperature = data.path("temperature").asInt(-999);
        String condition = data.path("condition").asText("unknown");

        cityWeatherData
                .computeIfAbsent(city, k -> new TreeMap<>())
                .put(date, temperature + "°C, " + condition);

        messageCount++;
        int printEvery = 9;
        if (messageCount >= printEvery) {
            tablePrinted = false;
            printTable();
            messageCount = 0;
        }
    }

    private void printTable() {
        if (tablePrinted) return;

        System.out.println();

        List<String> cities = new ArrayList<>(cityWeatherData.keySet());
        Collections.sort(cities);

        String lastDate = cityWeatherData.values().stream()
                .flatMap(map -> map.keySet().stream())
                .sorted()
                .reduce((a, b) -> b)
                .orElse("N/A");

        AsciiTable at = new AsciiTable();

        List<AT_Cell> headerCells = new ArrayList<>();
        headerCells.add(new AT_Cell("Дата"));
        for (String city : cities) {
            headerCells.add(new AT_Cell(city));
        }
        at.addRow(headerCells);
        at.addRule();

        List<AT_Cell> tempCells = new ArrayList<>();
        tempCells.add(new AT_Cell(lastDate));
        for (String city : cities) {
            String raw = cityWeatherData.getOrDefault(city, new TreeMap<>()).get(lastDate);
            String temp = extractTemp(raw);
            tempCells.add(new AT_Cell(temp));
        }
        at.addRow(tempCells);
        at.addRule();

        List<AT_Cell> conditionCells = new ArrayList<>();
        conditionCells.add(new AT_Cell(" "));
        for (String city : cities) {
            String raw = cityWeatherData.getOrDefault(city, new TreeMap<>()).get(lastDate);
            String condition = extractCondition(raw);
            conditionCells.add(new AT_Cell(condition));
        }
        at.addRow(conditionCells);
        at.addRule();

        CWC_LongestLine cwc = new CWC_LongestLine();
        at.getRenderer().setCWC(cwc);

        String rendered = at.render();
        System.out.println(rendered);

        tablePrinted = true;
    }

    private String extractTemp(String raw) {
        if (raw == null || !raw.contains("°C")) return "-";
        return raw.split(",")[0].trim();
    }

    private String extractCondition(String raw) {
        if (raw == null || !raw.contains(",")) return "-";
        return raw.split(",")[1].trim();
    }

}
