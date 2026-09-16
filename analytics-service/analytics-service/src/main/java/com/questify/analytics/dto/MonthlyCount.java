package com.questify.analytics.dto;

/** Number of items in a calendar month, {@code month} formatted as yyyy-MM. */
public record MonthlyCount(String month, long count) {
}
