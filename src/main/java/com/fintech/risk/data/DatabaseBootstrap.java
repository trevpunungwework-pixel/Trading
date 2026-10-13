package com.fintech.risk.data;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Ensures H2 schema and ARFF seed data exist before training or serving. */
@Component
public class DatabaseBootstrap {

  private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrap.class);

  private final ArffToDatabaseLoader loader;

  public DatabaseBootstrap(ArffToDatabaseLoader loader) {
    this.loader = loader;
  }

  @PostConstruct
  public void init() throws Exception {
    loader.ensureLoaded();
    log.info("Database bootstrap complete");
  }
}
