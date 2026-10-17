package com.example.School.Management.System.assistant;

import com.example.School.Management.System.exception.SqlRejectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SqlGuard {

    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|truncate|create|grant|revoke|merge|replace|call|exec|execute|"
                    + "into|outfile|dumpfile|load_file|sleep|benchmark|pg_sleep|information_schema|pg_catalog|mysql)\\b",
            Pattern.CASE_INSENSITIVE);

    private final SchemaProvider schema;

    /** Returns the cleaned query or throws SqlRejectedException. */
    public String check(String raw) {
        if (raw == null || raw.isBlank()) throw new SqlRejectedException("Empty query");

        String sql = raw.replaceAll("(?i)```(sql)?", "").trim();      // remove markdown fences
        sql = sql.replaceAll(";+\\s*$", "").trim();                    // remove final semicolon

        if (sql.contains(";"))                                   throw new SqlRejectedException("Only one statement is allowed");
        if (sql.contains("--") || sql.contains("/*") || sql.contains("#"))
            throw new SqlRejectedException("Comments are not allowed");
        if (!sql.toLowerCase().startsWith("select"))             throw new SqlRejectedException("Only SELECT queries are allowed");
        if (FORBIDDEN.matcher(sql).find())                       throw new SqlRejectedException("The query uses a forbidden keyword");

        Set<String> blocked = schema.blockedTables();
        for (String word : sql.toLowerCase().split("[^a-z0-9_]+"))
            if (blocked.contains(word))                          throw new SqlRejectedException("Table '" + word + "' is not available");

        return sql;
    }
}