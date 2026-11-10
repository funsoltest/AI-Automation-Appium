package com.practiceapi.aiclient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI ke plain-text response ko {filename: code} map mein parse karta hai.
 * Agar AI ne ### FILE: format follow nahi kiya, to poora response
 * ek default file maan liya jata hai.
 */
public class ResponseParser {

    private static final Pattern FILE_MARKER = Pattern.compile("###\\s*FILE:\\s*(.+?)\\s*\\r?\\n", Pattern.CASE_INSENSITIVE);

    public static String cleanCode(String code) {
        code = code.strip();
        code = code.replaceFirst("^```[a-zA-Z]*\\n?", "");
        code = code.replaceFirst("\\n?```$", "");
        return code.strip();
    }

    public static Map<String, String> parseFiles(String responseText, String defaultFilename) {
        Matcher matcher = FILE_MARKER.matcher(responseText);

        List<String> filenames = new ArrayList<>();
        List<Integer> contentStarts = new ArrayList<>();
        List<Integer> markerStarts = new ArrayList<>();

        while (matcher.find()) {
            filenames.add(matcher.group(1).trim());
            markerStarts.add(matcher.start());
            contentStarts.add(matcher.end());
        }

        Map<String, String> files = new LinkedHashMap<>();

        if (filenames.isEmpty()) {
            files.put(defaultFilename, cleanCode(responseText));
            return files;
        }

        for (int i = 0; i < filenames.size(); i++) {
            int contentStart = contentStarts.get(i);
            int contentEnd = (i + 1 < filenames.size()) ? markerStarts.get(i + 1) : responseText.length();
            String code = responseText.substring(contentStart, contentEnd);
            files.put(filenames.get(i), cleanCode(code));
        }

        return files;
    }
}