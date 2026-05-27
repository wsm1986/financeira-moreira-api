package com.financeira.api.application.dto;

import java.util.List;
import java.util.Map;

public record ImportPortalResponse(
        Map<String, EntityStats> summary,
        List<String> warnings
) {
    public record EntityStats(int imported, int skipped) {}
}
