package com.comix.scrapers.bedetheque.client.model.serie;

public enum SerieLanguage {

    UNAVAILABLE("", null),
    FRENCH("Français", "france.png"),
    ENGLISH("Anglais", "usa.png"),
    JAPANESE("Japonais", "japan.png"),
    ITALIAN("Italien", "italy.png"),
    GERMAN("Allemand", "germany.png"),
    SPANISH("Espagnol", "spain.png"),
    DUTCH("Neerlandais", "netherlands.png"),
    PORTUGUESE("Portugais", "portugal.png"),
    OTHER("Autre", "autre.png");

    private final String code;
    private final String flag;

    SerieLanguage(String code, String flag) {
        this.code = code;
        this.flag = flag;
    }

    @Override
    public String toString() {
        return code;
    }

    public static SerieLanguage fromValue(String t) {
        for(SerieLanguage s : SerieLanguage.values()) {
            if(String.valueOf(s.code).equals(t)) {
                return s;
            }
        }
        return SerieLanguage.UNAVAILABLE;
    }

    public static SerieLanguage fromFlag(String t) {
        for(SerieLanguage s : SerieLanguage.values()) {
            if(t.toLowerCase().contains(String.valueOf(s.flag))) {
                return s;
            }
        }
        return SerieLanguage.UNAVAILABLE;
    }

    public String getFlag() {
        return flag;
    }
}
