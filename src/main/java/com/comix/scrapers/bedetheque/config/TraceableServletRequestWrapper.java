package com.comix.scrapers.bedetheque.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

public class TraceableServletRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> headerMap;

    public TraceableServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.headerMap = new HashMap<>();
    }

    public void addHeader(String name, String value) {
        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        // Si l'en-tête est dans notre map, on retourne sa valeur.
        // Sinon, on délègue à la requête originale. C'est plus efficace.
        if (headerMap.containsKey(name)) {
            return headerMap.get(name);
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        // Utiliser un Set pour garantir l'unicité des noms d'en-tête
        Set<String> names = new HashSet<>(headerMap.keySet());
        names.addAll(Collections.list(super.getHeaderNames()));
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        // Comportement de remplacement cohérent avec getHeader()
        if (headerMap.containsKey(name)) {
            // Si l'en-tête est surchargé, on ne retourne que la nouvelle valeur.
            return Collections.enumeration(Collections.singletonList(headerMap.get(name)));
        }
        return super.getHeaders(name);
    }
}