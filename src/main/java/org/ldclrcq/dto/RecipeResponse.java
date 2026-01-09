package org.ldclrcq.dto;

import java.util.List;
import java.util.UUID;

public record RecipeResponse(
        UUID id,
        String name,
        int pageNumber,
        UUID cookbookId,
        String cookbookTitle,
        String cookbookAuthor,
        List<String> ingredients
) {}
