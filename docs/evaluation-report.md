# Credit risk model evaluation

Generated at: 2026-10-13T09:39:59.511233003Z

Stratified 10-fold cross-validation, seed 42. Positive class: **bad**.
Cost matrix: approve-bad (FN) = 5, decline-good (FP) = 1.
SMOTE applied to training folds only. Prep: ReplaceMissingValues → NominalToBinary → Normalize inside FilteredClassifier.

| Model | TP | FP | TN | FN | Recall(bad) | Precision(bad) | ROC AUC | Gini | KS | Total cost |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Logistic | 204 | 167 | 533 | 96 | 0.6800 | 0.5499 | 0.7814 | 0.5628 | 0.4629 | 647.0 |
| J48 | 157 | 151 | 549 | 143 | 0.5233 | 0.5097 | 0.6402 | 0.2804 | 0.3195 | 866.0 |
| RandomForest | 149 | 82 | 618 | 151 | 0.4967 | 0.6450 | 0.7909 | 0.5819 | 0.4414 | 837.0 |
| NaiveBayes | 210 | 202 | 498 | 90 | 0.7000 | 0.5097 | 0.7403 | 0.4806 | 0.4300 | 652.0 |

Champion for production scoring: **Logistic** (`models/credit-logit.model`).

