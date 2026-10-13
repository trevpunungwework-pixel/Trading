package com.fintech.risk.scoring;

import com.fintech.risk.data.JdbcInstancesRepository;
import com.fintech.risk.train.ModelPersistence;
import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Loads the champion model and a header Instances template for scoring.
 * Active in default (serve) mode; train mode builds the model instead.
 */
@Component
@Profile("!train")
public class ModelHolder {

  private static final Logger log = LoggerFactory.getLogger(ModelHolder.class);

  private final String modelPath;
  private final JdbcInstancesRepository instancesRepository;

  private Classifier classifier;
  private ModelPersistence.ModelMetadata metadata;
  private Instances header;

  public ModelHolder(
      @Value("${credit.risk.model-path}") String modelPath,
      JdbcInstancesRepository instancesRepository) {
    this.modelPath = modelPath;
    this.instancesRepository = instancesRepository;
  }

  @PostConstruct
  public void load() throws Exception {
    Path path = Path.of(modelPath);
    if (!Files.isRegularFile(path)) {
      throw new IllegalStateException(
          "Model not found at " + path.toAbsolutePath()
              + ". Run: java -jar credit-risk-scoring.jar train");
    }
    this.classifier = ModelPersistence.load(path);
    this.metadata = ModelPersistence.loadMetadata(path);
    Instances data = instancesRepository.loadAll();
    this.header = new Instances(data, 0);
    if (header.classIndex() < 0) {
      header.setClassIndex(header.numAttributes() - 1);
    }
    log.info("Loaded model {} trained at {}", metadata.modelName(), metadata.trainingDate());
  }

  public Classifier classifier() {
    return classifier;
  }

  public ModelPersistence.ModelMetadata metadata() {
    return metadata;
  }

  public Instances header() {
    return header;
  }
}
