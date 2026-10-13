package com.fintech.risk.integration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** In-memory stub so a real REST bureau client can replace it later. */
@Component
public class InMemoryCreditBureauLookup implements CreditBureauLookup {

  private final Map<String, BureauSummary> store = new ConcurrentHashMap<>();

  public InMemoryCreditBureauLookup() {
    store.put("demo-001", new BureauSummary("demo-001", 680, 3, false));
  }

  @Override
  public BureauSummary lookup(String applicantId) {
    return store.getOrDefault(
        applicantId, new BureauSummary(applicantId, 0, 0, false));
  }
}
