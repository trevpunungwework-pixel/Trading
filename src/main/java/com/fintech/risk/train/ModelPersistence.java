package com.fintech.risk.train;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import weka.classifiers.Classifier;
import weka.core.SerializationHelper;

/** Saves / loads the champion FilteredClassifier and sidecar metadata. */
public final class ModelPersistence {

  private ModelPersistence() {}

  public static void save(Classifier model, Path modelPath, ModelMetadata metadata) throws Exception {
    Files.createDirectories(modelPath.getParent() == null ? Path.of(".") : modelPath.getParent());
    SerializationHelper.write(modelPath.toString(), model);
    Path metaPath = metadataPath(modelPath);
    Files.writeString(metaPath, metadata.toPropertiesString());
  }

  public static Classifier load(Path modelPath) throws Exception {
    return (Classifier) SerializationHelper.read(modelPath.toString());
  }

  public static ModelMetadata loadMetadata(Path modelPath) throws Exception {
    Path metaPath = metadataPath(modelPath);
    if (!Files.isRegularFile(metaPath)) {
      return ModelMetadata.empty();
    }
    return ModelMetadata.fromPropertiesString(Files.readString(metaPath));
  }

  private static Path metadataPath(Path modelPath) {
    return modelPath.resolveSibling(modelPath.getFileName() + ".meta.properties");
  }

  public record ModelMetadata(
      String modelName,
      Instant trainingDate,
      double rocAuc,
      double gini,
      double ks,
      double recallBad,
      double precisionBad,
      double totalCost,
      String version) {

    static ModelMetadata empty() {
      return new ModelMetadata("unknown", Instant.EPOCH, 0, 0, 0, 0, 0, 0, "unknown");
    }

    static ModelMetadata fromChampion(EvaluationMetrics m, Instant when, String version) {
      return new ModelMetadata(
          m.modelName(), when, m.rocAuc(), m.gini(), m.ks(),
          m.recallBad(), m.precisionBad(), m.totalCost(), version);
    }

    String toPropertiesString() {
      return """
          modelName=%s
          trainingDate=%s
          rocAuc=%s
          gini=%s
          ks=%s
          recallBad=%s
          precisionBad=%s
          totalCost=%s
          version=%s
          """.formatted(
          modelName, trainingDate, rocAuc, gini, ks, recallBad, precisionBad, totalCost, version);
    }

    static ModelMetadata fromPropertiesString(String text) {
      java.util.Properties p = new java.util.Properties();
      try {
        p.load(new java.io.StringReader(text));
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
      return new ModelMetadata(
          p.getProperty("modelName", "unknown"),
          Instant.parse(p.getProperty("trainingDate", Instant.EPOCH.toString())),
          Double.parseDouble(p.getProperty("rocAuc", "0")),
          Double.parseDouble(p.getProperty("gini", "0")),
          Double.parseDouble(p.getProperty("ks", "0")),
          Double.parseDouble(p.getProperty("recallBad", "0")),
          Double.parseDouble(p.getProperty("precisionBad", "0")),
          Double.parseDouble(p.getProperty("totalCost", "0")),
          p.getProperty("version", "unknown"));
    }
  }
}
