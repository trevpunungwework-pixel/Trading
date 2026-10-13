package com.fintech.risk.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/** In-memory stub capturing scored events for later Kafka replacement. */
@Component
public class InMemoryLoanApplicationEvents implements LoanApplicationEvents {

  private final List<ScoredEvent> events = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void publishedScored(ScoredEvent event) {
    events.add(event);
  }

  public List<ScoredEvent> getEvents() {
    return List.copyOf(events);
  }
}
