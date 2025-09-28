package com.comix.scrapers.bedetheque.client.model.filter;

public enum SerieStatus {

    UNAVAILABLE(""),
    FINISHED("0"),
    CURRENT("1"),
    ONE_SHOT("2"),
    HANGING("3"),
    DISCONTINUED("4");

    private String code;

    SerieStatus(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    public static SerieStatus fromValue(String t) {
        for(SerieStatus s : SerieStatus.values()) {
            if(String.valueOf(s.code).equals(t)) {
                return s;
            }
        }
        return SerieStatus.UNAVAILABLE;
    }
}
