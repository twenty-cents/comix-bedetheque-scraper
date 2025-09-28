package com.comix.scrapers.bedetheque.client.model.filter;

public enum SerieOrigin {

    UNAVAILABLE(""),
    EUROPE("1"),
    ASIA("2"),
    USA("3"),
    OTHER("4");

    private String code;

    SerieOrigin(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    public static SerieOrigin fromValue(String t) {
        for(SerieOrigin s : SerieOrigin.values()) {
            if(String.valueOf(s.code).equals(t)) {
                return s;
            }
        }
        return SerieOrigin.UNAVAILABLE;
    }

}
