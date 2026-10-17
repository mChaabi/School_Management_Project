package com.example.School.Management.System.assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReadOnlyQueryRunner {

    private static final int MAX_ROWS = 100;
    private final JdbcTemplate jdbc;

    public QueryResult run(String sql) {
        return jdbc.execute((ConnectionCallback<QueryResult>) con -> {
            boolean previousReadOnly = con.isReadOnly();
            con.setReadOnly(true);
            try (Statement st = con.createStatement()) {
                st.setMaxRows(MAX_ROWS);
                st.setQueryTimeout(5);
                try (ResultSet rs = st.executeQuery(sql)) {
                    ResultSetMetaData md = rs.getMetaData();
                    int colCount = md.getColumnCount();
                    List<String> cols = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        cols.add(md.getColumnLabel(i));
                    }
                    List<List<String>> rows = new ArrayList<>();
                    while (rs.next()) {
                        List<String> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            Object v = rs.getObject(i);
                            row.add(v == null ? "" : v.toString());
                        }
                        rows.add(row);
                    }
                    boolean truncated = rows.size() >= MAX_ROWS;
                    return new QueryResult(cols, rows, truncated);
                }
            } finally {
                con.setReadOnly(previousReadOnly);
            }
        });
    }
}
