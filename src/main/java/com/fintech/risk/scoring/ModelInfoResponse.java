package com.fintech.risk.scoring;

import java.time.Instant;
import java.util.Map;

public record ModelInfoResponse(
    String modelName,
    Instant trainingDate,
    String modelVersion,
    Map<String, Double> crossValidationMetrics) {}
