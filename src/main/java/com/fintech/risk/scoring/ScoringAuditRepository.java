package com.fintech.risk.scoring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class ScoringAuditRepository {

  private final DataSource dataSource;

  public ScoringAuditRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void insert(String requestJson, double pd, String decision, String modelVersion)
      throws Exception {
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(
             """
             INSERT INTO scoring_audit
               (request_json, probability_of_default, decision, model_version)
             VALUES (?, ?, ?, ?)
             """)) {
      ps.setString(1, requestJson);
      ps.setDouble(2, pd);
      ps.setString(3, decision);
      ps.setString(4, modelVersion);
      ps.executeUpdate();
    }
  }
}
