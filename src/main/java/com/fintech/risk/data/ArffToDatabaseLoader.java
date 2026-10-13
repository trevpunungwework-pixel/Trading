package com.fintech.risk.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 * Loads credit-g.arff into H2 table {@code loan_applications} on first start.
 * Subsequent starts skip reload when the table already has rows.
 */
@Component
public class ArffToDatabaseLoader {

  private static final Logger log = LoggerFactory.getLogger(ArffToDatabaseLoader.class);

  private final javax.sql.DataSource dataSource;
  private final String arffPath;

  public ArffToDatabaseLoader(
      javax.sql.DataSource dataSource,
      @Value("${credit.risk.arff-path}") String arffPath) {
    this.dataSource = dataSource;
    this.arffPath = arffPath;
  }

  public void ensureLoaded() throws Exception {
    try (Connection conn = dataSource.getConnection()) {
      createTableIfNeeded(conn);
      createAuditTableIfNeeded(conn);
      int count = countRows(conn);
      if (count > 0) {
        log.info("loan_applications already populated ({} rows); skipping ARFF load", count);
        return;
      }
      Path path = Path.of(arffPath);
      if (!Files.isRegularFile(path)) {
        throw new IllegalStateException("ARFF not found at " + path.toAbsolutePath());
      }
      Instances data = new ConverterUtils.DataSource(path.toString()).getDataSet();
      if (data.numInstances() != 1000) {
        throw new IllegalStateException("Expected 1000 instances, found " + data.numInstances());
      }
      insertAll(conn, data);
      log.info("Loaded {} rows from {} into {}", data.numInstances(), arffPath, Schema.TABLE);
    }
  }

  private void createTableIfNeeded(Connection conn) throws Exception {
    StringBuilder ddl = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
        .append(Schema.TABLE).append(" (id IDENTITY PRIMARY KEY");
    for (Schema.Column col : Schema.COLUMNS) {
      ddl.append(", \"").append(col.name()).append("\" ")
          .append(col.numeric() ? "DOUBLE" : "VARCHAR(128)");
    }
    ddl.append(')');
    try (Statement st = conn.createStatement()) {
      st.execute(ddl.toString());
    }
  }

  private void createAuditTableIfNeeded(Connection conn) throws Exception {
    try (Statement st = conn.createStatement()) {
      st.execute("""
          CREATE TABLE IF NOT EXISTS scoring_audit (
            id IDENTITY PRIMARY KEY,
            scored_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            request_json CLOB,
            probability_of_default DOUBLE,
            decision VARCHAR(32),
            model_version VARCHAR(64)
          )
          """);
    }
  }

  private int countRows(Connection conn) throws Exception {
    try (Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + Schema.TABLE)) {
      rs.next();
      return rs.getInt(1);
    }
  }

  private void insertAll(Connection conn, Instances data) throws Exception {
    StringBuilder sql = new StringBuilder("INSERT INTO ").append(Schema.TABLE).append(" (");
    for (int i = 0; i < Schema.COLUMNS.size(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      sql.append('"').append(Schema.COLUMNS.get(i).name()).append('"');
    }
    sql.append(") VALUES (");
    for (int i = 0; i < Schema.COLUMNS.size(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      sql.append('?');
    }
    sql.append(')');

    boolean prev = conn.getAutoCommit();
    conn.setAutoCommit(false);
    try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      for (int r = 0; r < data.numInstances(); r++) {
        var inst = data.instance(r);
        for (int c = 0; c < Schema.COLUMNS.size(); c++) {
          Schema.Column col = Schema.COLUMNS.get(c);
          if (inst.isMissing(c)) {
            ps.setObject(c + 1, null);
          } else if (col.numeric()) {
            ps.setDouble(c + 1, inst.value(c));
          } else {
            ps.setString(c + 1, inst.stringValue(c));
          }
        }
        ps.addBatch();
      }
      ps.executeBatch();
      conn.commit();
    } catch (Exception e) {
      conn.rollback();
      throw e;
    } finally {
      conn.setAutoCommit(prev);
    }
  }
}
