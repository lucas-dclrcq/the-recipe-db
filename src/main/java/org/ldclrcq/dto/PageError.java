package org.ldclrcq.dto;

public record PageError(
        int pageOrder,
        String errorMessage
) {}
