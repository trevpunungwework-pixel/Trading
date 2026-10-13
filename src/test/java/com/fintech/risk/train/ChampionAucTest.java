package com.fintech.risk.train;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fintech.risk.data.ArffToDatabaseLoader;
import com.fintech.risk.data.JdbcInstancesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import weka.core.Instances;

@SpringBootTest
@ActiveProfiles("train")
class ChampionAucTest {

  @Autowired
  private ArffToDatabaseLoader loader;
  @Autowired
  private JdbcInstancesRepository repository;

  @BeforeEach
  void seed() throws Exception {
    loader.ensureLoaded();
  }

  @Test
  void logisticCrossValidatedAucAboveThreshold() throws Exception {
    Instances raw = repository.loadAll();
    EvaluationMetrics metrics = CrossValidator.evaluate(ModelSpec.LOGISTIC, raw);
    assertTrue(metrics.rocAuc() > 0.65,
        "Champion Logistic CV AUC must be > 0.65, was " + metrics.rocAuc());
  }
}
