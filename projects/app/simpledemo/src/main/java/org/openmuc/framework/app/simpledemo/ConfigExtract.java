package org.openmuc.framework.app.simpledemo;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ConfigExtract {

    // Parse config text into a map of key -> value
    public static Map<String, String> parseConfig(String text) {
        Map<String, String> map = new LinkedHashMap<>();

        // matches: key=value  OR  key: value
        Pattern p = Pattern.compile("^\\s*([A-Za-z0-9_.-]+)\\s*[:=]\\s*(.*?)\\s*$");

        for (String line : text.split("\\R")) { // \\R = any line break
            String s = line.trim();
            if (s.isEmpty() || s.startsWith("#")) {
                continue;
            }
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String key = m.group(1).trim();
                String value = m.group(2).trim();
                map.put(key, value);
            }
        }
        return map;
    }

    public static Map<String, String> parseConfigFile(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        return parseConfig(text);
    }
}