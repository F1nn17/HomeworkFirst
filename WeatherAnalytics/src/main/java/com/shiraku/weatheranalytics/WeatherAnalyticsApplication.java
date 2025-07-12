package com.shiraku.weatheranalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WeatherAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherAnalyticsApplication.class, args);
	}

}
