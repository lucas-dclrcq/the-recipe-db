package org.ldclrcq.dto;

import java.time.Instant;
import java.util.UUID;

public record CookbookDetailResponse(
        UUID id,
        String title,
        String author,
        Instant createdAt,
        long recipeCount,
        boolean hasCover,
        String ocrStatus,
        String ocrErrorMessage
) {}
