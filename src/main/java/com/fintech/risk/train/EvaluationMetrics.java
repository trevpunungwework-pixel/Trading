package com.fintech.risk.train;

/** Cross-validation metrics for one model; positive class is always "bad". */
public record EvaluationMetrics(
    String modelName,
    int tp,
    int fp,
    int tn,
    int fn,
    double recallBad,
    double precisionBad,
    double rocAuc,
    double gini,
    double ks,
    double totalCost) {

  public static EvaluationMetrics fromCounts(
      String modelName,
      int tp,
      int fp,
      int tn,
      int fn,
      double rocAuc,
      double ks) {
    double recall = tp + fn == 0 ? 0.0 : (double) tp / (tp + fn);
    double precision = tp + fp == 0 ? 0.0 : (double) tp / (tp + fp);
    double gini = 2.0 * rocAuc - 1.0;
    // Cost matrix: approve bad (FN) = 5; decline good (FP) = 1
    double cost = 5.0 * fn + 1.0 * fp;
    return new EvaluationMetrics(
        modelName, tp, fp, tn, fn, recall, precision, rocAuc, gini, ks, cost);
  }
}
