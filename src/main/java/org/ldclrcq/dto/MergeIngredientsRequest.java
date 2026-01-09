package org.ldclrcq.dto;

import java.util.List;
import java.util.UUID;

public record MergeIngredientsRequest(
        UUID targetId,
        List<UUID> sourceIds
) {}
