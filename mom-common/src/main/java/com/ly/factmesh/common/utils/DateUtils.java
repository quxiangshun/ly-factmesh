package com.ly.factmesh.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern(PATTERN_DATE);
    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern(PATTERN_DATETIME);

    private DateUtils() {
    }

    public static String format(LocalDate date) {
        return date != null ? date.format(FMT_DATE) : null;
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FMT_DATETIME) : null;
    }

    public static LocalDate parseDate(String str) {
        return str != null && !str.isBlank() ? LocalDate.parse(str.trim(), FMT_DATE) : null;
    }

    public static LocalDateTime parseDateTime(String str) {
        return str != null && !str.isBlank() ? LocalDateTime.parse(str.trim(), FMT_DATETIME) : null;
    }
}
