package com.fintech.risk.data;

import java.util.List;

/** Shared column metadata for loan_applications (mirrors credit-g.arff). */
public final class Schema {

  public static final String TABLE = "loan_applications";
  public static final String CLASS_COLUMN = "class";
  /** Positive class everywhere. */
  public static final String POSITIVE_CLASS = "bad";
  public static final String NEGATIVE_CLASS = "good";

  public static final List<Column> COLUMNS = List.of(
      Column.nominal("checking_status"),
      Column.numeric("duration"),
      Column.nominal("credit_history"),
      Column.nominal("purpose"),
      Column.numeric("credit_amount"),
      Column.nominal("savings_status"),
      Column.nominal("employment"),
      Column.numeric("installment_commitment"),
      Column.nominal("personal_status"),
      Column.nominal("other_parties"),
      Column.numeric("residence_since"),
      Column.nominal("property_magnitude"),
      Column.numeric("age"),
      Column.nominal("other_payment_plans"),
      Column.nominal("housing"),
      Column.numeric("existing_credits"),
      Column.nominal("job"),
      Column.numeric("num_dependents"),
      Column.nominal("own_telephone"),
      Column.nominal("foreign_worker"),
      Column.nominal(CLASS_COLUMN)
  );

  private Schema() {}

  public record Column(String name, boolean numeric) {
    static Column numeric(String name) {
      return new Column(name, true);
    }

    static Column nominal(String name) {
      return new Column(name, false);
    }
  }
}
