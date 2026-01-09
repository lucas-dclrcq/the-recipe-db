package org.ldclrcq.dto;

public record OcrResultDto(
        String ingredient,
        String recipeName,
        int pageNumber,
        double confidence,
        boolean needsReview
) {}
