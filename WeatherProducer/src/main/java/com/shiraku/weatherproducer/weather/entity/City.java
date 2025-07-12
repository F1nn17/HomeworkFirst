package com.shiraku.weatherproducer.weather.entity;

import lombok.Getter;

@Getter
public enum City {
    MOSCOW("Москва"),
    SAINT_PETERSBURG("Санкт-Петербург"),
    NOVOSIBIRSK("Новосибирск"),
    KAZAN("Казань"),
    SAMARA("Самара"),
    SARATOV("Саратов"),
    MAGADAN("Магадан"),
    CHUKOTKA("Чукотка"),
    TUMEN("Тюмень"),
    ROSTOV("Ростов")
    ;

    private final String displayName;

    City(String displayName) {
        this.displayName = displayName;
    }

    public static City getRandomCity() {
        City[] cities = values();
        int index = (int) (Math.random() * cities.length);
        return cities[index];
    }

}
