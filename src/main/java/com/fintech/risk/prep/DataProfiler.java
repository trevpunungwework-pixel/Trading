package com.fintech.risk.prep;

import com.fintech.risk.data.Schema;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

/** Profiles loan_applications with Tablesaw and writes docs/data-profile.txt. */
@Component
public class DataProfiler {

  private final DataSource dataSource;

  public DataProfiler(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Path writeProfile(Path output) throws Exception {
    Table table;
    try (Connection conn = dataSource.getConnection();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT * FROM " + Schema.TABLE)) {
      table = Table.read().db(rs, Schema.TABLE);
    }

    List<String> lines = new ArrayList<>();
    lines.add("German Credit — loan_applications profile (Tablesaw)");
    lines.add("=====================================================");
    lines.add("row_count=" + table.rowCount());
    lines.add("column_count=" + table.columnCount());
    lines.add("");
    lines.add("Missing values per column:");
    for (String name : table.columnNames()) {
      int missing = table.column(name).countMissing();
      lines.add("  " + name + "=" + missing);
    }
    lines.add("");
    lines.add("Class balance (\"" + Schema.CLASS_COLUMN + "\"):");
    try (Connection conn = dataSource.getConnection();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(
             "SELECT \"" + Schema.CLASS_COLUMN + "\", COUNT(*) AS n FROM "
                 + Schema.TABLE + " GROUP BY \"" + Schema.CLASS_COLUMN + "\" ORDER BY 1")) {
      while (rs.next()) {
        lines.add("  " + rs.getString(1) + "=" + rs.getInt(2));
      }
    }

    Files.createDirectories(output.getParent());
    Files.writeString(output, String.join(System.lineSeparator(), lines) + System.lineSeparator());
    return output;
  }
}
