package com.fintech.risk.prep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fintech.risk.data.ArffToDatabaseLoader;
import com.fintech.risk.data.JdbcInstancesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import weka.core.Instances;
import weka.filters.MultiFilter;

@SpringBootTest
@ActiveProfiles("train")
class PreparationPipelineTest {

  @Autowired
  private ArffToDatabaseLoader loader;
  @Autowired
  private JdbcInstancesRepository repository;

  @BeforeEach
  void seed() throws Exception {
    loader.ensureLoaded();
  }

  @Test
  void preparationLeavesNoMissingValues() throws Exception {
    Instances raw = repository.loadAll();
    MultiFilter prep = PreparationPipeline.createPrepFilter();
    Instances prepared = PreparationPipeline.applyFilter(prep, raw);

    assertTrue(prepared.numInstances() > 0);
    int missing = 0;
    for (int i = 0; i < prepared.numInstances(); i++) {
      for (int a = 0; a < prepared.numAttributes(); a++) {
        if (prepared.instance(i).isMissing(a)) {
          missing++;
        }
      }
    }
    assertEquals(0, missing, "Prepared instances must have no missing values");
  }
}
