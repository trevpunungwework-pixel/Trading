package com.fintech.risk.train;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;

@Service
public class ModelTrainingService {

  private static final Logger log = LoggerFactory.getLogger(ModelTrainingService.class);

  private final String modelPath;
  private final String modelVersion;

  public ModelTrainingService(
      @Value("${credit.risk.model-path}") String modelPath,
      @Value("${credit.risk.model-version}") String modelVersion) {
    this.modelPath = modelPath;
    this.modelVersion = modelVersion;
  }

  public void trainAndEvaluate(Instances raw) throws Exception {
    List<EvaluationMetrics> all = new ArrayList<>();
    for (ModelSpec spec : ModelSpec.values()) {
      log.info("Cross-validating {} ...", spec.displayName());
      EvaluationMetrics metrics = CrossValidator.evaluate(spec, raw);
      all.add(metrics);
      log.info("{} AUC={} KS={} cost={}",
          metrics.modelName(), metrics.rocAuc(), metrics.ks(), metrics.totalCost());
    }

    Instant trainedAt = Instant.now();
    Path report = EvaluationReportWriter.write(Path.of("docs/evaluation-report.md"), all, trainedAt);
    log.info("Wrote evaluation report to {}", report.toAbsolutePath());

    EvaluationMetrics championMetrics = all.stream()
        .filter(m -> ModelSpec.LOGISTIC.displayName().equals(m.modelName()))
        .findFirst()
        .orElseThrow();

    FilteredClassifier champion = CrossValidator.trainChampion(raw);
    ModelPersistence.ModelMetadata meta =
        ModelPersistence.ModelMetadata.fromChampion(championMetrics, trainedAt, modelVersion);
    ModelPersistence.save(champion, Path.of(modelPath), meta);
    log.info("Saved champion model to {}", Path.of(modelPath).toAbsolutePath());
  }
}
