package com.unyaunya.minic.preprocess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.unyaunya.minic.Location;
import com.unyaunya.minic.MinicException;

public class Preprocessor {
    public static class FileRegion {
        public final String filename;
        public final int startLine; // 1-based
        public final int endLine;   // inclusive

        public FileRegion(String filename, int startLine, int endLine) {
            this.filename = filename;
            this.startLine = startLine;
            this.endLine = endLine;
        }
    }

    public static class Result {
        public final String content;
        private final List<FileRegion> regions;

        public Result(String content, List<FileRegion> regions) {
            this.content = content;
            this.regions = regions;
        }

        public Location getLocation(int combinedLine) {
            for (FileRegion r : regions) {
                if (combinedLine >= r.startLine && combinedLine <= r.endLine) {
                    int local = combinedLine - r.startLine + 1;
                    return new Location(r.filename, local);
                }
            }
            return new Location(null, combinedLine);
        }
    }

    private final Set<Path> visited = new HashSet<>();
    private final List<FileRegion> regions = new ArrayList<>();
    private final StringBuilder out = new StringBuilder();
    private int currentLine = 1; // 1-based

    public Result preprocess(Path path) throws IOException {
        visited.clear();
        regions.clear();
        out.setLength(0);
        currentLine = 1;
        includeFile(path);
        return new Result(out.toString(), new ArrayList<>(regions));
    }

    private void includeFile(Path path) throws IOException {
        Path abs = path.toAbsolutePath().normalize();
        if (visited.contains(abs)) return; // prevent cycles
        visited.add(abs);

        List<String> lines = Files.readAllLines(abs);
        if (lines.isEmpty()) {
            return;
        }
        int start = currentLine;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.startsWith("#include")) {
                // flush previous region up to previous line if needed
                if (currentLine > start) {
                    regions.add(new FileRegion(abs.toString(), start, currentLine - 1));
                    start = currentLine;
                }
                String inc = extractIncludePath(trimmed);
                if (inc == null) {
                    throw new MinicException("Include directive does not specify a valid file path", new Location(abs.toString(), i + 1));
                }
                Path included = abs.getParent().resolve(inc).normalize();
                includeFile(included);
                // after include, continue
            } else {
                out.append(line).append('\n');
                currentLine++;
            }
        }
        // finalize region for this file
        if (currentLine > start) {
            regions.add(new FileRegion(abs.toString(), start, currentLine - 1));
        }
    }

    private String extractIncludePath(String line) {
        // accept #include "file"
        int firstQuote = line.indexOf('"');
        int lastQuote = line.lastIndexOf('"');
        if (firstQuote >= 0 && lastQuote > firstQuote) {
            return line.substring(firstQuote + 1, lastQuote);
        }
        return null;
    }
}
