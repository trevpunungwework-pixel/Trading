package com.fintech.risk.train;

import com.fintech.risk.data.Schema;
import com.fintech.risk.prep.Nd4jFeatureStats;
import com.fintech.risk.prep.PreparationPipeline;
import java.util.Random;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.MultiFilter;

/**
 * Stratified 10-fold CV with seed 42.
 * Each fold uses {@link SmoteFilteredClassifier}: prep learned on the train fold,
 * SMOTE on that prepared train fold only, evaluate on the raw test fold (filtered at predict time).
 */
public final class CrossValidator {

  public static final int FOLDS = 10;
  public static final int SEED = 42;

  private CrossValidator() {}

  public static EvaluationMetrics evaluate(ModelSpec spec, Instances raw) throws Exception {
    Instances data = new Instances(raw);
    if (data.classIndex() < 0) {
      data.setClassIndex(data.numAttributes() - 1);
    }
    data.randomize(new Random(SEED));
    data.stratify(FOLDS);

    int posIndex = data.classAttribute().indexOfValue(Schema.POSITIVE_CLASS);
    if (posIndex < 0) {
      throw new IllegalStateException("Positive class '" + Schema.POSITIVE_CLASS + "' not found");
    }

    int tp = 0;
    int fp = 0;
    int tn = 0;
    int fn = 0;
    double[] allScores = new double[data.numInstances()];
    boolean[] allPos = new boolean[data.numInstances()];
    int scoreIdx = 0;

    for (int fold = 0; fold < FOLDS; fold++) {
      Instances train = data.trainCV(FOLDS, fold, new Random(SEED));
      Instances test = data.testCV(FOLDS, fold);

      SmoteFilteredClassifier fc = new SmoteFilteredClassifier();
      fc.setFilter(PreparationPipeline.createPrepFilter());
      fc.setClassifier(spec.create());
      fc.buildClassifier(train);

      for (int i = 0; i < test.numInstances(); i++) {
        Instance inst = test.instance(i);
        double pred = fc.classifyInstance(inst);
        double[] dist = fc.distributionForInstance(inst);
        double score = dist[posIndex];
        allScores[scoreIdx] = score;
        boolean actualPos = ((int) inst.classValue()) == posIndex;
        allPos[scoreIdx] = actualPos;
        scoreIdx++;

        boolean predictedPos = ((int) pred) == posIndex;
        if (actualPos && predictedPos) {
          tp++;
        } else if (!actualPos && predictedPos) {
          fp++;
        } else if (!actualPos && !predictedPos) {
          tn++;
        } else {
          fn++;
        }
      }
    }

    double auc = computeAuc(allScores, allPos);
    double ks = Nd4jFeatureStats.kolmogorovSmirnov(allScores, allPos);

    MultiFilter sampleFilter = PreparationPipeline.createPrepFilter();
    Instances samplePrep = PreparationPipeline.applyFilter(sampleFilter, data);
    var matrix = Nd4jFeatureStats.toFeatureMatrix(samplePrep);
    Nd4jFeatureStats.featureMeans(matrix);
    Nd4jFeatureStats.featureStds(matrix);

    return EvaluationMetrics.fromCounts(spec.displayName(), tp, fp, tn, fn, auc, ks);
  }

  /** Train champion on all data (prep + SMOTE + Logistic). */
  public static FilteredClassifier trainChampion(Instances raw) throws Exception {
    Instances data = new Instances(raw);
    if (data.classIndex() < 0) {
      data.setClassIndex(data.numAttributes() - 1);
    }
    SmoteFilteredClassifier fc = new SmoteFilteredClassifier();
    fc.setFilter(PreparationPipeline.createPrepFilter());
    fc.setClassifier(ModelSpec.LOGISTIC.create());
    fc.buildClassifier(data);
    return fc;
  }

  /** Mann–Whitney AUC from predicted P(bad). */
  static double computeAuc(double[] scores, boolean[] positive) {
    int nPos = 0;
    int nNeg = 0;
    for (boolean p : positive) {
      if (p) {
        nPos++;
      } else {
        nNeg++;
      }
    }
    if (nPos == 0 || nNeg == 0) {
      return 0.5;
    }

    Integer[] order = new Integer[scores.length];
    for (int i = 0; i < order.length; i++) {
      order[i] = i;
    }
    java.util.Arrays.sort(order, (a, b) -> Double.compare(scores[a], scores[b]));

    double rankSumPos = 0.0;
    int i = 0;
    while (i < order.length) {
      int j = i;
      while (j + 1 < order.length && scores[order[j + 1]] == scores[order[i]]) {
        j++;
      }
      double avgRank = (i + 1 + j + 1) / 2.0;
      for (int k = i; k <= j; k++) {
        if (positive[order[k]]) {
          rankSumPos += avgRank;
        }
      }
      i = j + 1;
    }
    return (rankSumPos - (nPos * (nPos + 1) / 2.0)) / (nPos * (double) nNeg);
  }
}
