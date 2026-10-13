package com.fintech.risk.train;

import com.fintech.risk.prep.PreparationPipeline;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.Capabilities;
import weka.filters.Filter;

/**
 * FilteredClassifier that applies the configured prep filter, then SMOTE on the
 * training data only, then builds the base classifier. At scoring time only the
 * prep filter runs (no SMOTE).
 */
public class SmoteFilteredClassifier extends FilteredClassifier {

  private static final long serialVersionUID = 1L;

  @Override
  public void buildClassifier(Instances data) throws Exception {
    getCapabilities().testWithFail(data);

    Instances train = new Instances(data);
    train.deleteWithMissingClass();

    Filter filter = getFilter();
    if (filter == null) {
      throw new IllegalStateException("Prep filter must be set");
    }
    filter.setInputFormat(train);
    Instances prepared = Filter.useFilter(train, filter);
    m_FilteredInstances = prepared.stringFreeStructure();

    Instances smoted = PreparationPipeline.applySmote(prepared);

    Classifier base = getClassifier();
    if (base == null) {
      throw new IllegalStateException("Base classifier must be set");
    }
    base.buildClassifier(smoted);
  }

  @Override
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();
    result.enableAll();
    return result;
  }
}
