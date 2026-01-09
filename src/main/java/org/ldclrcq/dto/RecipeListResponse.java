package org.ldclrcq.dto;

import java.util.List;

public record RecipeListResponse(
        List<RecipeResponse> recipes,
        String nextCursor,
        boolean hasMore
) {}
