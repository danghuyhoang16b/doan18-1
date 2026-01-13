package com.example.app.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringHelper {
    public static String removeAccents(String s) {
        if (s == null) return null;
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ','d').replace('Đ','D');
    }

    public static boolean containsIgnoreCase(String source, String query) {
        if (source == null || query == null) return false;
        String s = removeAccents(source).toLowerCase();
        String q = removeAccents(query).toLowerCase();
        return s.contains(q);
    }
}