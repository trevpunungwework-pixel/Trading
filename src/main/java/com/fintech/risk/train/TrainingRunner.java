package com.fintech.risk.train;

import com.fintech.risk.data.JdbcInstancesRepository;
import com.fintech.risk.prep.DataProfiler;
import com.fintech.risk.prep.Nd4jFeatureStats;
import com.fintech.risk.prep.PreparationPipeline;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import weka.core.Instances;
import weka.filters.MultiFilter;

/**
 * Orchestrates train-mode: load from H2, profile, prepare, evaluate, persist champion.
 */
@Component
public class TrainingRunner {

  private static final Logger log = LoggerFactory.getLogger(TrainingRunner.class);

  private final JdbcInstancesRepository instancesRepository;
  private final DataProfiler dataProfiler;
  private final ModelTrainingService modelTrainingService;

  public TrainingRunner(
      JdbcInstancesRepository instancesRepository,
      DataProfiler dataProfiler,
      ModelTrainingService modelTrainingService) {
    this.instancesRepository = instancesRepository;
    this.dataProfiler = dataProfiler;
    this.modelTrainingService = modelTrainingService;
  }

  public void runTraining() throws Exception {
    Path profilePath = dataProfiler.writeProfile(Path.of("docs/data-profile.txt"));
    log.info("Wrote data profile to {}", profilePath.toAbsolutePath());

    Instances raw = instancesRepository.loadAll();
    log.info("Loaded {} instances from H2 (class index={})", raw.numInstances(), raw.classIndex());

    MultiFilter prep = PreparationPipeline.createPrepFilter();
    Instances prepared = PreparationPipeline.applyFilter(prep, raw);
    log.info("Prepared features: attrs={} ({})", prepared.numAttributes(),
        Nd4jFeatureStats.summarize(prepared));

    // SMOTE demo on full set only for logging balance; CV applies SMOTE per train fold
    Instances smoted = PreparationPipeline.applySmote(prepared);
    log.info("SMOTE on full prepared set: {} -> {} instances", prepared.numInstances(),
        smoted.numInstances());

    modelTrainingService.trainAndEvaluate(raw);
  }
}
