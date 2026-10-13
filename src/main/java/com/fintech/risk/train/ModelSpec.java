package com.fintech.risk.train;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

/** Named base classifiers used in the champion/challenger suite. */
public enum ModelSpec {
  LOGISTIC("Logistic", true),
  J48("J48", false),
  RANDOM_FOREST("RandomForest", false),
  NAIVE_BAYES("NaiveBayes", false);

  private final String displayName;
  private final boolean champion;

  ModelSpec(String displayName, boolean champion) {
    this.displayName = displayName;
    this.champion = champion;
  }

  public String displayName() {
    return displayName;
  }

  public boolean champion() {
    return champion;
  }

  public Classifier create() throws Exception {
    return switch (this) {
      case LOGISTIC -> new Logistic();
      case J48 -> new J48();
      case RANDOM_FOREST -> {
        RandomForest rf = new RandomForest();
        rf.setNumIterations(200);
        yield rf;
      }
      case NAIVE_BAYES -> new NaiveBayes();
    };
  }

  public Classifier copy() throws Exception {
    return AbstractClassifier.makeCopy(create());
  }
}
