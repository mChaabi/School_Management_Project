package com.example.School.Management.System.assistant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaProvider {

    private static final Set<String> ENUM_LIKE = Set.of("gender", "status", "day_of_week");

    private final DataSource dataSource;

    @Value("${app.assistant.tables:students,teachers,courses,classrooms,enrollments,schedules,attendances}")
    private List<String> allowedTables;

    private String schemaText;
    private String dialect;
    private final Set<String> blocked = new HashSet<>();

    public synchronized String schema()            { load(); return schemaText; }
    public synchronized String dialect()           { load(); return dialect; }
    public synchronized Set<String> blockedTables(){ load(); return blocked; }

    private void load() {
        if (schemaText != null) return;
        Set<String> allowed = allowedTables.stream()
                .map(t -> t.trim().toLowerCase()).collect(Collectors.toSet());
        StringBuilder sb = new StringBuilder();

        try (Connection con = dataSource.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            dialect = md.getDatabaseProductName();

            List<String> names = new ArrayList<>();
            try (ResultSet rs = md.getTables(con.getCatalog(), con.getSchema(), "%", new String[]{"TABLE"})) {
                while (rs.next()) names.add(rs.getString("TABLE_NAME"));
            }

            for (String name : names) {
                String lower = name.toLowerCase();
                if (!allowed.contains(lower)) { blocked.add(lower); continue; }   // never exposed

                List<String[]> cols = new ArrayList<>();
                try (ResultSet c = md.getColumns(con.getCatalog(), con.getSchema(), name, "%")) {
                    while (c.next()) cols.add(new String[]{c.getString("COLUMN_NAME"), c.getString("TYPE_NAME")});
                }

                sb.append("TABLE ").append(lower).append(" (\n");
                for (String[] col : cols) {
                    sb.append("  ").append(col[0].toLowerCase()).append(' ').append(col[1].toLowerCase());
                    if (ENUM_LIKE.contains(col[0].toLowerCase()))
                        sb.append("   -- values: ").append(distinct(con, name, col[0]));
                    sb.append('\n');
                }
                try (ResultSet fk = md.getImportedKeys(con.getCatalog(), con.getSchema(), name)) {
                    while (fk.next())
                        sb.append("  FK: ").append(fk.getString("FKCOLUMN_NAME").toLowerCase())
                                .append(" -> ").append(fk.getString("PKTABLE_NAME").toLowerCase())
                                .append('.').append(fk.getString("PKCOLUMN_NAME").toLowerCase()).append('\n');
                }
                sb.append(")\n\n");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read the database structure", e);
        }

        schemaText = sb.toString();
        log.info("🤖 [ASSISTANT] schema given to the AI:\n{}", schemaText);
    }

    private String distinct(Connection con, String table, String col) {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT " + col + " FROM " + table
                     + " WHERE " + col + " IS NOT NULL LIMIT 12")) {
            List<String> v = new ArrayList<>();
            while (rs.next()) v.add(rs.getString(1));
            return v.isEmpty() ? "(no data yet)" : String.join(", ", v);
        } catch (SQLException e) {
            return "(unknown)";
        }
    }
}
