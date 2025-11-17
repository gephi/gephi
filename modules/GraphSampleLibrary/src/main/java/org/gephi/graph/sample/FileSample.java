package org.gephi.graph.sample;

public record FileSample(String filepath,
                         String description,
                         String author,
                         String license) {
    public String fullDescription() {
        StringBuilder buf = new StringBuilder(description);
        if (author != null && !author.isBlank()) {
            buf.append(" (")
                .append(author)
                .append(")");
        }
        if (license != null && !license.isBlank()) {
            buf.append(" --")
                .append(license);
        }
        return buf.toString();
    }
}
