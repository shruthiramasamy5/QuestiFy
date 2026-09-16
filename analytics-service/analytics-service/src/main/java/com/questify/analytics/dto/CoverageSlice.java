package com.questify.analytics.dto;

/** Share of questions/papers attributed to one CO or Bloom level. */
public record CoverageSlice(String label, long count, double percentage) {
}
