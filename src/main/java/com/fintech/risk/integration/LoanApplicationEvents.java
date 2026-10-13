package com.fintech.risk.integration;

/**
 * Publishes loan-application lifecycle events.
 * Production adapters (Kafka) can replace the in-memory stub — Kafka itself is not on the classpath.
 */
public interface LoanApplicationEvents {

  void publishedScored(ScoredEvent event);

  record ScoredEvent(String applicationId, double probabilityOfDefault, String decision, String modelVersion) {}
}
