package org.ldclrcq.dto;

import java.util.UUID;

public record UploadIndexPagesResponse(
        UUID cookbookId,
        int pageCount
) {}
