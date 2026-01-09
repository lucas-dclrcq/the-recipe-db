package org.ldclrcq.dto;

import java.util.UUID;

public record IngredientResponse(
        UUID id,
        String name,
        long recipeCount
) {}
