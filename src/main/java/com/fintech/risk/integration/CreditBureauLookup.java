package com.fintech.risk.integration;

/**
 * Lookup interface for external credit-bureau data.
 * Production adapters (REST) can replace the in-memory stub.
 */
public interface CreditBureauLookup {

  /**
   * @param applicantId external applicant identifier
   * @return bureau summary, or empty if unknown
   */
  BureauSummary lookup(String applicantId);

  record BureauSummary(String applicantId, int bureauScore, int openTradelines, boolean recentDelinquency) {}
}
