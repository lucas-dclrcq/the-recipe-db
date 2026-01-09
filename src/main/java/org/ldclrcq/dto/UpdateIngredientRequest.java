package org.ldclrcq.dto;

import java.util.List;

public record UpdateIngredientRequest(
        String name,
        List<String> disambiguations,
        List<Integer> availableMonths
) {}
