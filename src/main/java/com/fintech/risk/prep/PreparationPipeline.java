package com.fintech.risk.prep;

import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.supervised.instance.SMOTE;
import weka.core.Instances;

/**
 * Builds the Weka preparation chain and applies SMOTE to training data only.
 */
public final class PreparationPipeline {

  private PreparationPipeline() {}

  /** ReplaceMissingValues → NominalToBinary → Normalize. */
  public static MultiFilter createPrepFilter() {
    MultiFilter multi = new MultiFilter();
    multi.setFilters(new Filter[] {
        new ReplaceMissingValues(),
        new NominalToBinary(),
        new Normalize()
    });
    return multi;
  }

  public static Instances applyFilter(Filter filter, Instances data) throws Exception {
    filter.setInputFormat(data);
    return Filter.useFilter(data, filter);
  }

  /** SMOTE on labelled training instances; never call on held-out test data. */
  public static Instances applySmote(Instances train) throws Exception {
    SMOTE smote = new SMOTE();
    smote.setInputFormat(train);
    return Filter.useFilter(train, smote);
  }

  /** Fit prep filter on train, transform train and test (no SMOTE on test). */
  public static PreparedFold prepareFold(Instances train, Instances test) throws Exception {
    MultiFilter filter = createPrepFilter();
    filter.setInputFormat(train);
    Instances trainPrep = Filter.useFilter(train, filter);
    Instances testPrep = Filter.useFilter(test, filter);
    Instances trainSmote = applySmote(trainPrep);
    return new PreparedFold(trainSmote, testPrep, filter);
  }

  public record PreparedFold(Instances train, Instances test, MultiFilter fittedFilter) {}
}
