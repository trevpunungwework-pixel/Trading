package com.fintech.risk.prep;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import weka.core.Instances;

/**
 * Converts prepared numeric features to ND4J arrays and supports KS calculation helpers.
 */
public final class Nd4jFeatureStats {

  private Nd4jFeatureStats() {}

  /** Rows = instances, columns = non-class numeric attributes (all attrs after prep are numeric). */
  public static INDArray toFeatureMatrix(Instances data) {
    int rows = data.numInstances();
    int cols = data.classIndex() >= 0 ? data.numAttributes() - 1 : data.numAttributes();
    INDArray matrix = Nd4j.zeros(rows, cols);
    for (int i = 0; i < rows; i++) {
      int colOut = 0;
      for (int a = 0; a < data.numAttributes(); a++) {
        if (a == data.classIndex()) {
          continue;
        }
        double v = data.instance(i).isMissing(a) ? Double.NaN : data.instance(i).value(a);
        matrix.putScalar(i, colOut++, v);
      }
    }
    return matrix;
  }

  public static INDArray featureMeans(INDArray features) {
    return features.mean(0);
  }

  public static INDArray featureStds(INDArray features) {
    INDArray std = features.std(true, true, 0);
    // avoid divide-by-zero
    return Transforms.max(std, Nd4j.scalar(1e-12));
  }

  /**
   * Two-sample Kolmogorov–Smirnov statistic between score distributions of the two classes.
   * {@code scores} length = n, {@code classIsPositive} marks positive ("bad") instances.
   */
  public static double kolmogorovSmirnov(double[] scores, boolean[] classIsPositive) {
    int nPos = 0;
    int nNeg = 0;
    for (boolean p : classIsPositive) {
      if (p) {
        nPos++;
      } else {
        nNeg++;
      }
    }
    if (nPos == 0 || nNeg == 0) {
      return 0.0;
    }
    double[] pos = new double[nPos];
    double[] neg = new double[nNeg];
    int ip = 0;
    int in = 0;
    for (int i = 0; i < scores.length; i++) {
      if (classIsPositive[i]) {
        pos[ip++] = scores[i];
      } else {
        neg[in++] = scores[i];
      }
    }
    java.util.Arrays.sort(pos);
    java.util.Arrays.sort(neg);

    // Merge unique thresholds from both samples
    double maxDiff = 0.0;
    int i = 0;
    int j = 0;
    while (i < pos.length || j < neg.length) {
      double threshold;
      if (i < pos.length && (j >= neg.length || pos[i] <= neg[j])) {
        threshold = pos[i];
      } else {
        threshold = neg[j];
      }
      while (i < pos.length && pos[i] <= threshold) {
        i++;
      }
      while (j < neg.length && neg[j] <= threshold) {
        j++;
      }
      double cdfPos = (double) i / nPos;
      double cdfNeg = (double) j / nNeg;
      maxDiff = Math.max(maxDiff, Math.abs(cdfPos - cdfNeg));
    }
    return maxDiff;
  }

  /** Convenience: means/stds of prepared features (documented side effect of ND4J usage). */
  public static String summarize(Instances prepared) {
    INDArray x = toFeatureMatrix(prepared);
    INDArray mean = featureMeans(x);
    INDArray std = featureStds(x);
    return "nd4j_features=" + x.columns() + " mean0=" + mean.getDouble(0)
        + " std0=" + std.getDouble(0);
  }
}
