package com.fintech.risk.train;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Writes docs/evaluation-report.md from actual CV results (never hand-typed metrics). */
public final class EvaluationReportWriter {

  private EvaluationReportWriter() {}

  public static Path write(Path output, List<EvaluationMetrics> metrics, Instant trainedAt)
      throws Exception {
    List<String> lines = new ArrayList<>();
    lines.add("# Credit risk model evaluation");
    lines.add("");
    lines.add("Generated at: " + trainedAt);
    lines.add("");
    lines.add("Stratified 10-fold cross-validation, seed 42. Positive class: **bad**.");
    lines.add("Cost matrix: approve-bad (FN) = 5, decline-good (FP) = 1.");
    lines.add("SMOTE applied to training folds only. Prep: ReplaceMissingValues → NominalToBinary → Normalize inside FilteredClassifier.");
    lines.add("");
    lines.add("| Model | TP | FP | TN | FN | Recall(bad) | Precision(bad) | ROC AUC | Gini | KS | Total cost |");
    lines.add("| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |");
    for (EvaluationMetrics m : metrics) {
      lines.add(String.format(Locale.ROOT,
          "| %s | %d | %d | %d | %d | %.4f | %.4f | %.4f | %.4f | %.4f | %.1f |",
          m.modelName(), m.tp(), m.fp(), m.tn(), m.fn(),
          m.recallBad(), m.precisionBad(), m.rocAuc(), m.gini(), m.ks(), m.totalCost()));
    }
    lines.add("");
    lines.add("Champion for production scoring: **Logistic** (`models/credit-logit.model`).");
    lines.add("");

    Files.createDirectories(output.getParent() == null ? Path.of(".") : output.getParent());
    Files.writeString(output, String.join(System.lineSeparator(), lines) + System.lineSeparator());
    return output;
  }
}
