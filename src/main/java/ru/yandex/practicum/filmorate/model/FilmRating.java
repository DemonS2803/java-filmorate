package ru.yandex.practicum.filmorate.model;

import java.util.HashMap;
import java.util.Map;

public enum FilmRating {

    G(1, "G", "0+"),
    PG(2, "PG", "6+"),
    PG_13(3, "PG-13", "12+"),
    R(4, "R", "16+"),
    NC_17(5, "NC-17", "18+"),;

    private static final Map<Integer, FilmRating> map = new HashMap<>();

    static {
        map.put(1, G);
        map.put(2, PG);
        map.put(3, PG_13);
        map.put(4, R);
        map.put(5, NC_17);
    }

    private final Integer id;
    private final String us;
    private final String ru;

    FilmRating(Integer id, String us, String ru) {
        this.id = id;
        this.us = us;
        this.ru = ru;
    }

    public static FilmRating valueOf(int value) {
        return map.get(value);
    }

    public static Integer getIdFor(FilmRating rating) {
        for (Integer key : map.keySet()) {
            if (map.get(key) == rating) {
                return key;
            }
        }
        // case mustn't ever happen
        return 0;
    }

    public Integer getId() {
        return id;
    }

    public String getUs() {
        return us;
    }

    public String getRu() {
        return ru;
    }
}
