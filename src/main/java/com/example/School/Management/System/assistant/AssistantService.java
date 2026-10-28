package com.example.School.Management.System.assistant;

import com.example.School.Management.System.exception.SqlRejectedException;
import com.example.School.Management.System.prompt.PromptTemplates;
import com.example.School.Management.System.service.AiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantService {

    private final AiClient ai;
    private final PromptTemplates prompts;
    private final SchemaProvider schema;
    private final SqlGuard guard;
    private final ReadOnlyQueryRunner runner;

    public AssistantAnswer ask(String question) {
        // 1. The AI writes the query, guided by the schema and the rules in assistant_sql.txt
        String sqlSystem = prompts.render(prompts.load("assistant_sql"), Map.of(
                "dialect", schema.dialect(),
                "today", LocalDate.now().toString(),
                "ageHint", ageHint(schema.dialect()),
                "schema", schema.schema()));

        // line 34: write the query
        Optional<String> first = ai.complete(sqlSystem, "QUESTION: " + question, 1500, 0.0);
        if (first.isEmpty())
            return AssistantAnswer.message("The AI service is not reachable right now. Please try again in a moment.");

        String raw = first.get().trim();
        if (raw.toUpperCase().startsWith("NO_QUERY"))
            return AssistantAnswer.message(raw.replaceFirst("(?i)NO_QUERY:?\\s*", ""));

        // 2. Check and run. If it fails, the AI gets the error and tries once more
        String sql = null;
        QueryResult result = null;
        String error = null;
        for (int attempt = 0; attempt < 2 && result == null; attempt++) {
            try {
                raw = raw.replaceAll("(?is)^```(?:sql)?\\s*", "")
                        .replaceAll("(?is)\\s*```$", "")
                        .trim();
                sql = guard.check(raw);
                result = runner.run(sql);
            } catch (SqlRejectedException e) {
                error = e.getMessage();
            } catch (DataAccessException e) {
                error = e.getMostSpecificCause().getMessage();
            }
            if (result == null && attempt == 0) {
                log.warn("🤖 [ASSISTANT] attempt 1 failed: {} | {}", error, raw);
                // retry after an error
                raw = ai.complete(sqlSystem, "QUESTION: " + question
                        + "\nYOUR PREVIOUS SQL:\n" + raw
                        + "\nIT FAILED WITH: " + error
                        + "\nReturn one corrected SELECT query only.", 1500, 0.0).orElse("");
            }
        }
        if (result == null) {
            log.warn("🤖 [ASSISTANT] gave up: {} | {}", error, raw);
            return AssistantAnswer.message("I could not build a reliable query for that question. "
                    + "Try rephrasing it with more detail.");
        }
        log.info("🤖 [ASSISTANT] question='{}' rows={} sql={}", question, result.rows().size(), sql);

        // 3. The AI explains the real result. If it fails, the data is still shown
        // 3. explain the result
        String answer = ai.complete(prompts.load("assistant_answer"),
                        "QUESTION: " + question
                                + "\nROWS RETURNED: " + result.rows().size() + (result.truncated() ? "+" : "")
                                + "\nRESULT:\n" + asText(result), 800, 0.2)
                .map(String::trim).filter(t -> !t.isBlank())
                .orElse(result.rows().size() + " row(s) found. See the data below.");

        return new AssistantAnswer(answer, sql, result.columns(), result.rows(), result.truncated());
    }

    private String asText(QueryResult r) {
        StringBuilder sb = new StringBuilder(String.join("\t", r.columns())).append('\n');
        r.rows().stream().limit(60).forEach(row -> sb.append(String.join("\t", row)).append('\n'));
        return sb.toString();
    }

    private String ageHint(String dialect) {
        String d = dialect.toLowerCase();
        if (d.contains("mysql") || d.contains("mariadb")) return "TIMESTAMPDIFF(YEAR, birth_date, CURDATE())";
        if (d.contains("postgres"))                       return "DATE_PART('year', AGE(birth_date))::int";
        return "DATEDIFF('YEAR', birth_date, CURRENT_DATE)";
    }
}