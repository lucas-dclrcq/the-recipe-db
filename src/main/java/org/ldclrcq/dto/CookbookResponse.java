package org.ldclrcq.dto;

import java.time.Instant;
import java.util.UUID;

public record CookbookResponse(
        UUID id,
        String title,
        String author,
        Instant createdAt
) {}
