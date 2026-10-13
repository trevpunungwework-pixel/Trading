package com.fintech.risk.scoring;

public record ScoreResponse(
    double probabilityOfDefault,
    String decision,
    String modelVersion) {}
