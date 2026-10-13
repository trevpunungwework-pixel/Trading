package com.fintech.risk.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

/**
 * Reads training data from H2 via Weka {@link InstanceQuery} (not from the ARFF file).
 */
@Repository
public class JdbcInstancesRepository {

  private final String jdbcUrl;
  private final String username;
  private final String password;

  public JdbcInstancesRepository(
      @Value("${spring.datasource.url}") String jdbcUrl,
      @Value("${spring.datasource.username}") String username,
      @Value("${spring.datasource.password:}") String password) {
    this.jdbcUrl = jdbcUrl;
    this.username = username;
    this.password = password == null ? "" : password;
  }

  public Instances loadAll() throws Exception {
    ensureDatabaseUtilsProps();
    InstanceQuery query = new InstanceQuery();
    query.setDatabaseURL(jdbcUrl);
    query.setUsername(username);
    query.setPassword(password);
    // Quote identifiers; exclude surrogate key
    StringBuilder cols = new StringBuilder();
    for (int i = 0; i < Schema.COLUMNS.size(); i++) {
      if (i > 0) {
        cols.append(", ");
      }
      cols.append('"').append(Schema.COLUMNS.get(i).name()).append('"');
    }
    query.setQuery("SELECT " + cols + " FROM " + Schema.TABLE);
    Instances data = query.retrieveInstances();
    query.close();
    if (data.classIndex() < 0) {
      data.setClassIndex(data.numAttributes() - 1);
    }
    return data;
  }

  /**
   * InstanceQuery resolves DatabaseUtils.props from the working directory first.
   * Always refresh from the packaged H2 mappings so type names like CHARACTER VARYING resolve.
   */
  private void ensureDatabaseUtilsProps() throws Exception {
    Path cwdProps = Path.of("DatabaseUtils.props");
    ClassPathResource res = new ClassPathResource("weka/experiment/DatabaseUtils.props");
    if (!res.exists()) {
      res = new ClassPathResource("DatabaseUtils.props");
    }
    Files.copy(res.getInputStream(), cwdProps, StandardCopyOption.REPLACE_EXISTING);
  }
}
