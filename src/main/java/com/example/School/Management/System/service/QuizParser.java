package com.example.School.Management.System.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QuizParser {
    private QuizParser() {}

    public record Parsed(String text, List<String> options, int correct) {}
    public record Result(List<Parsed> questions, List<String> warnings) {}

    private static final Pattern Q = Pattern.compile(
            "^\\s*(?:Q(?:uestion)?\\s*)?(\\d{1,3})\\s*[.):\\-]\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPT = Pattern.compile("^\\s*\\(?([A-Ha-h])[.)]\\s+(.+)$");
    private static final Pattern ANS = Pattern.compile(
            "^\\s*(?:answer|ans|correct|r[ée]ponse|respuesta)\\s*[:=\\-]\\s*\\(?([A-Ha-h])\\)?\\s*$",
            Pattern.CASE_INSENSITIVE);

    private static final class Draft {
        int no; StringBuilder text = new StringBuilder();
        List<StringBuilder> opts = new ArrayList<>(); int correct = -1;
    }

    public static Result parse(String content) {
        List<Parsed> out = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Draft cur = null;

        for (String raw : content.split("\\R")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            Matcher a = ANS.matcher(line);
            if (cur != null && a.matches()) { cur.correct = Character.toUpperCase(a.group(1).charAt(0)) - 'A'; continue; }

            Matcher q = Q.matcher(line);
            if (q.matches()) {
                flush(cur, out, warnings);
                cur = new Draft(); cur.no = Integer.parseInt(q.group(1)); cur.text.append(q.group(2).trim());
                continue;
            }
            if (cur == null) continue;

            Matcher o = OPT.matcher(line);
            if (o.matches()) {
                String t = o.group(2).trim();
                if (t.endsWith("*")) { cur.correct = cur.opts.size(); t = t.substring(0, t.length() - 1).trim(); }
                else if (t.toLowerCase().endsWith("(correct)")) { cur.correct = cur.opts.size(); t = t.substring(0, t.length() - 9).trim(); }
                cur.opts.add(new StringBuilder(t));
            } else if (cur.opts.isEmpty()) {
                cur.text.append(' ').append(line);                                  // question on several lines
            } else {
                cur.opts.get(cur.opts.size() - 1).append(' ').append(line);        // option on several lines
            }
        }
        flush(cur, out, warnings);
        return new Result(out, warnings);
    }

    private static void flush(Draft d, List<Parsed> out, List<String> warnings) {
        if (d == null) return;
        if (d.opts.size() < 2)                          { warnings.add("Question " + d.no + " skipped: it needs at least 2 options."); return; }
        if (d.correct < 0 || d.correct >= d.opts.size()) { warnings.add("Question " + d.no + " skipped: no correct answer marked."); return; }
        out.add(new Parsed(d.text.toString(), d.opts.stream().map(StringBuilder::toString).toList(), d.correct));
    }
}