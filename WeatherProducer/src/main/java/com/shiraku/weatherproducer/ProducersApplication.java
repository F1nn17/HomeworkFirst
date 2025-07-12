package com.shiraku.weatherproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ProducersApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducersApplication.class, args);
    }

}
