package org.ldclrcq.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IngredientDetailResponse(
        UUID id,
        String name,
        List<String> disambiguations,
        long recipeCount,
        List<RecipeSummary> recipes,
        List<Integer> availableMonths,
        Instant createdAt,
        Instant updatedAt
) {
    public record RecipeSummary(
            UUID id,
            String name,
            String cookbookTitle,
            Integer pageNumber
    ) {}
}
