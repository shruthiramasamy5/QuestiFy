package com.questify.approval.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;

/** Optional comment supplied when approving or rejecting. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DecisionRequest(
        @JsonAlias({"comments", "comment"})
        @Size(max = 1000)
        String comment
) {
}
