package com.cobip.domain.grammar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse.ExecutionFlowStep;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse.OutputSnapshot;
import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse.VariableSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class JavaRuntimeExecutionFlowTracer {

    static final String TRACE_PREFIX = "__COBIP_TRACE__";

    private static final int MAX_TRACE_EVENTS = 300;
    private static final Pattern METHOD_START_PATTERN = Pattern.compile(".*\\)\\s*(?:throws\\s+[^{]+)?\\{\\s*$");
    private static final Pattern FOR_VARIABLE_PATTERN = Pattern.compile(
            "^for\\s*\\(\\s*(?:final\\s+)?(?:int|long|short|byte|var)\\s+([A-Za-z_$][A-Za-z0-9_$]*)\\s*=.*"
    );
    private static final Pattern OUTPUT_PATTERN = Pattern.compile("^(\\s*)System\\.out\\.(print|println)\\((.*)\\)(\\s*;\\s*)$");
    private static final Pattern ARRAY_ASSIGNMENT_PATTERN = Pattern.compile(
            "^([A-Za-z_$][A-Za-z0-9_$]*)\\s*\\[(.+)]\\s*=\\s*(.+?)\\s*;?$"
    );
    private static final Pattern COLLECTION_MUTATION_PATTERN = Pattern.compile(
            "^([A-Za-z_$][A-Za-z0-9_$]*)\\.(set|add|remove)\\((.*)\\)\\s*;?$"
    );
    private static final Pattern DECLARATION_PATTERN = Pattern.compile(
            "^(?:final\\s+)?([A-Za-z_$][A-Za-z0-9_$]*(?:<[^>]+>)?(?:\\s*\\[\\])?)\\s+"
                    + "([A-Za-z_$][A-Za-z0-9_$]*)\\s*=\\s*(.+?)\\s*;?$"
    );
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile(
            "^([A-Za-z_$][A-Za-z0-9_$]*)\\s*=\\s*(.+?)\\s*;?$"
    );
    private static final String HELPER_SOURCE = """

class __CobipTrace {
    private static final String PREFIX = "__COBIP_TRACE__";
    private static final int LIMIT = 300;
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final StringBuilder output = new StringBuilder();
    private static int count = 0;

    static void line(int line) {
        emit("{\\"type\\":\\"LINE\\",\\"lineNumber\\":" + line + "}");
    }

    static void loop(int line) {
        emit("{\\"type\\":\\"LOOP\\",\\"lineNumber\\":" + line + "}");
    }

    static void condition(int line) {
        emit("{\\"type\\":\\"CONDITION\\",\\"lineNumber\\":" + line + "}");
    }

    static void variable(int line, String name, Object value, String dataType) {
        emit("{\\"type\\":\\"VARIABLE\\",\\"lineNumber\\":" + line
                + ",\\"name\\":" + quote(name)
                + ",\\"value\\":" + quote(formatValue(value))
                + dataTypeField(dataType)
                + elementsField(value)
                + "}");
    }

    static void array(int line, String name, Object value, int index) {
        emit("{\\"type\\":\\"ARRAY_UPDATE\\",\\"lineNumber\\":" + line
                + ",\\"name\\":" + quote(name)
                + ",\\"value\\":" + quote(formatValue(value))
                + ",\\"index\\":" + index
                + elementsField(value)
                + "}");
    }

    static void collection(int line, String name, Object value, Object index) {
        emit("{\\"type\\":\\"COLLECTION_UPDATE\\",\\"lineNumber\\":" + line
                + ",\\"name\\":" + quote(name)
                + ",\\"value\\":" + quote(formatValue(value))
                + indexField(index)
                + elementsField(value)
                + "}");
    }

    static void print(int line, Object value) {
        emitOutput(line, formatValue(value), "");
    }

    static void println(int line) {
        emitOutput(line, "", LINE_SEPARATOR);
    }

    static void println(int line, Object value) {
        emitOutput(line, formatValue(value), LINE_SEPARATOR);
    }

    private static void emitOutput(int line, String value, String suffix) {
        output.append(value).append(suffix);
        emit("{\\"type\\":\\"OUTPUT\\",\\"lineNumber\\":" + line
                + ",\\"value\\":" + quote(value)
                + ",\\"output\\":" + quote(output.toString())
                + "}");
    }

    private static void emit(String json) {
        if (count >= LIMIT) {
            return;
        }
        count++;
        System.out.println(PREFIX + json);
    }

    private static String dataTypeField(String dataType) {
        return dataType == null ? "" : ",\\"dataType\\":" + quote(dataType);
    }

    private static String elementsField(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Iterable<?>) {
            return ",\\"elements\\":" + iterableElements((Iterable<?>) value);
        }
        if (!value.getClass().isArray()) {
            return "";
        }
        return ",\\"elements\\":" + arrayElements(value);
    }

    private static String indexField(Object index) {
        return index instanceof Number ? ",\\"index\\":" + index : "";
    }

    private static String iterableElements(Iterable<?> value) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Object element : value) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append(quote(String.valueOf(element)));
        }
        builder.append(']');
        return builder.toString();
    }

    private static String arrayElements(Object value) {
        int length = java.lang.reflect.Array.getLength(value);
        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(quote(String.valueOf(java.lang.reflect.Array.get(value, index))));
        }
        builder.append(']');
        return builder.toString();
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (!value.getClass().isArray()) {
            return String.valueOf(value);
        }
        StringBuilder builder = new StringBuilder("[");
        int length = java.lang.reflect.Array.getLength(value);
        for (int index = 0; index < length; index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(String.valueOf(java.lang.reflect.Array.get(value, index)));
        }
        builder.append(']');
        return builder.toString();
    }

    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\\"" + value
                .replace("\\\\", "\\\\\\\\")
                .replace("\\"", "\\\\\\"")
                .replace("\\r", "\\\\r")
                .replace("\\n", "\\\\n")
                + "\\"";
    }
}
""";

    private final ObjectMapper objectMapper = new ObjectMapper();

    String instrument(String sourceCode) {
        String[] lines = sourceCode.split("\\R", -1);
        List<String> instrumentedLines = new ArrayList<>();
        int methodDepth = 0;
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            String trimmedLine = line.strip();
            boolean insideMethod = methodDepth > 0;
            if (insideMethod && isTraceableLine(trimmedLine)) {
                instrumentLine(instrumentedLines, line, trimmedLine, index + 1);
            } else {
                instrumentedLines.add(line);
            }

            if (insideMethod) {
                methodDepth = Math.max(0, methodDepth + braceDelta(line));
            } else if (isMethodStart(trimmedLine)) {
                methodDepth = Math.max(0, braceDelta(line));
            }
        }
        return String.join(System.lineSeparator(), instrumentedLines) + HELPER_SOURCE;
    }

    List<ExecutionFlowStep> buildSteps(String sourceCode, String stdout) {
        List<JsonNode> events = traceEvents(stdout);
        if (events.isEmpty()) {
            return List.of();
        }

        String[] sourceLines = sourceCode.split("\\R", -1);
        Map<String, VariableSnapshot> variables = new LinkedHashMap<>();
        List<OutputSnapshot> outputs = new ArrayList<>();
        List<ExecutionFlowStep> steps = new ArrayList<>();

        for (JsonNode event : events) {
            if (!shouldExposeEvent(event)) {
                continue;
            }
            int stepOrder = steps.size() + 1;
            int lineNumber = event.path("lineNumber").asInt(1);
            String sourceLine = sourceLine(sourceLines, lineNumber);
            String eventType = eventType(event.path("type").asText());
            VariableSnapshot activeVariable = variableSnapshot(event, variables, sourceLine, lineNumber, stepOrder)
                    .orElse(null);
            if (activeVariable != null) {
                variables.put(activeVariable.getName(), activeVariable);
            }

            OutputSnapshot activeOutput = outputSnapshot(event, sourceLine, lineNumber, stepOrder).orElse(null);
            if (activeOutput != null) {
                outputs.add(activeOutput);
            }

            steps.add(new ExecutionFlowStep(
                    stepOrder,
                    lineNumber,
                    sourceLine,
                    eventType,
                    description(eventType),
                    activeVariable,
                    activeOutput,
                    new ArrayList<>(variables.values()),
                    outputs
            ));
        }
        return steps;
    }

    private void instrumentLine(List<String> instrumentedLines, String line, String trimmedLine, int lineNumber) {
        Optional<String> outputLine = outputLine(line, lineNumber);
        if (outputLine.isPresent()) {
            instrumentedLines.add(outputLine.get());
            return;
        }

        String blockLine = blockLine(line, trimmedLine, lineNumber);
        Optional<String> assignmentTrace = assignmentTraceLine(line, trimmedLine, lineNumber);
        if (assignmentTrace.isEmpty() && blockLine.equals(line)) {
            instrumentedLines.add(indentOf(line) + "__CobipTrace.line(" + lineNumber + ");");
        }
        instrumentedLines.add(blockLine);
        assignmentTrace.ifPresent(instrumentedLines::add);
    }

    private Optional<String> outputLine(String line, int lineNumber) {
        Matcher matcher = OUTPUT_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String indent = matcher.group(1);
        String method = matcher.group(2);
        String expression = matcher.group(3).strip();
        String suffix = matcher.group(4);
        if ("println".equals(method) && expression.isBlank()) {
            return Optional.of(indent + "__CobipTrace.println(" + lineNumber + ")" + suffix);
        }
        if (expression.isBlank()) {
            return Optional.of(indent + "__CobipTrace.print(" + lineNumber + ", \"\")" + suffix);
        }
        return Optional.of(indent + "__CobipTrace." + method + "(" + lineNumber + ", " + expression + ")" + suffix);
    }

    private String blockLine(String line, String trimmedLine, int lineNumber) {
        if (isLoopLine(trimmedLine) && line.contains("{")) {
            String loopVariableTrace = loopVariableTrace(trimmedLine, lineNumber);
            String trace = loopVariableTrace.isBlank()
                    ? "__CobipTrace.loop(" + lineNumber + ");"
                    : loopVariableTrace;
            return injectAfterOpeningBrace(line, trace);
        }
        if (isConditionLine(trimmedLine) && line.contains("{")) {
            return injectAfterOpeningBrace(line, "__CobipTrace.condition(" + lineNumber + ");");
        }
        return line;
    }

    private String loopVariableTrace(String trimmedLine, int lineNumber) {
        Matcher matcher = FOR_VARIABLE_PATTERN.matcher(trimmedLine);
        if (!matcher.matches()) {
            return "";
        }
        String name = matcher.group(1);
        return "__CobipTrace.variable(" + lineNumber + ", \"" + name + "\", " + name + ", \"loop\");";
    }

    private Optional<String> assignmentTraceLine(String line, String trimmedLine, int lineNumber) {
        if (containsComparisonOperator(trimmedLine) || trimmedLine.startsWith("for ") || trimmedLine.startsWith("for(")) {
            return Optional.empty();
        }
        String indent = indentOf(line);
        Matcher arrayMatcher = ARRAY_ASSIGNMENT_PATTERN.matcher(trimmedLine);
        if (arrayMatcher.matches()) {
            String name = arrayMatcher.group(1);
            String indexExpression = arrayMatcher.group(2);
            return Optional.of(indent + "__CobipTrace.array(" + lineNumber + ", \"" + name + "\", "
                    + name + ", " + indexExpression + ");");
        }

        Matcher collectionMatcher = COLLECTION_MUTATION_PATTERN.matcher(trimmedLine);
        if (collectionMatcher.matches()) {
            String name = collectionMatcher.group(1);
            String method = collectionMatcher.group(2);
            String indexExpression = collectionMutationIndexExpression(method, collectionMatcher.group(3));
            return Optional.of(indent + "__CobipTrace.collection(" + lineNumber + ", \"" + name + "\", "
                    + name + ", " + indexExpression + ");");
        }

        Matcher declarationMatcher = DECLARATION_PATTERN.matcher(trimmedLine);
        if (declarationMatcher.matches()) {
            String dataType = declarationMatcher.group(1).replaceAll("\\s+", "");
            String name = declarationMatcher.group(2);
            return Optional.of(indent + "__CobipTrace.variable(" + lineNumber + ", \"" + name + "\", "
                    + name + ", \"" + dataType + "\");");
        }

        Matcher assignmentMatcher = ASSIGNMENT_PATTERN.matcher(trimmedLine);
        if (assignmentMatcher.matches()) {
            String name = assignmentMatcher.group(1);
            return Optional.of(indent + "__CobipTrace.variable(" + lineNumber + ", \"" + name + "\", "
                    + name + ", null);");
        }
        return Optional.empty();
    }

    private String collectionMutationIndexExpression(String method, String arguments) {
        List<String> parts = splitArguments(arguments);
        if ("set".equals(method) || "remove".equals(method)) {
            return parts.isEmpty() ? "null" : parts.getFirst();
        }
        if ("add".equals(method) && parts.size() > 1) {
            return parts.getFirst();
        }
        return "null";
    }

    private List<JsonNode> traceEvents(String stdout) {
        if (stdout == null || stdout.isBlank()) {
            return List.of();
        }
        List<JsonNode> events = new ArrayList<>();
        for (String line : stdout.split("\\R")) {
            if (!line.startsWith(TRACE_PREFIX)) {
                continue;
            }
            try {
                events.add(objectMapper.readTree(line.substring(TRACE_PREFIX.length())));
            } catch (Exception ignored) {
                // Ignore malformed trace rows and keep any valid trace emitted before it.
            }
            if (events.size() >= MAX_TRACE_EVENTS) {
                break;
            }
        }
        return events;
    }

    private boolean shouldExposeEvent(JsonNode event) {
        if (!"OUTPUT".equals(event.path("type").asText())) {
            return true;
        }
        String value = event.path("value").asText("");
        if (!value.isBlank()) {
            return true;
        }
        String output = event.path("output").asText("");
        return output.endsWith("\n") || output.endsWith("\r");
    }

    private Optional<VariableSnapshot> variableSnapshot(
        JsonNode event,
        Map<String, VariableSnapshot> variables,
        String sourceLine,
        int lineNumber,
        int stepOrder
    ) {
        String type = event.path("type").asText();
        if (!"VARIABLE".equals(type) && !"ARRAY_UPDATE".equals(type) && !"COLLECTION_UPDATE".equals(type)) {
            return Optional.empty();
        }
        String name = event.path("name").asText();
        if (name.isBlank()) {
            return Optional.empty();
        }
        List<String> elements = elements(event.path("elements"));
        String value = event.path("value").asText(elements.isEmpty() ? "" : "[" + String.join(", ", elements) + "]");
        String changeType = variables.containsKey(name) ? "UPDATED" : "CREATED";
        Integer activeIndex = event.has("index") ? event.path("index").asInt() : null;
        return Optional.of(new VariableSnapshot(
                name,
                value,
                sourceLine.strip(),
                textOrNull(event.path("dataType")),
                changeType,
                lineNumber,
                stepOrder,
                elements,
                activeIndex
        ));
    }

    private Optional<OutputSnapshot> outputSnapshot(JsonNode event, String sourceLine, int lineNumber, int stepOrder) {
        if (!"OUTPUT".equals(event.path("type").asText())) {
            return Optional.empty();
        }
        return Optional.of(new OutputSnapshot(
                event.has("output") ? event.path("output").asText() : event.path("value").asText(),
                sourceLine.strip(),
                lineNumber,
                stepOrder
        ));
    }

    private List<String> elements(JsonNode elementsNode) {
        if (!elementsNode.isArray()) {
            return List.of();
        }
        List<String> elements = new ArrayList<>();
        for (JsonNode element : elementsNode) {
            elements.add(element.asText());
        }
        return elements;
    }

    private String textOrNull(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private String sourceLine(String[] sourceLines, int lineNumber) {
        if (lineNumber < 1 || lineNumber > sourceLines.length) {
            return "";
        }
        return sourceLines[lineNumber - 1];
    }

    private String eventType(String traceType) {
        return switch (traceType) {
            case "OUTPUT" -> "OUTPUT";
            case "CONDITION" -> "CONDITION";
            case "LOOP" -> "LOOP";
            case "VARIABLE", "ARRAY_UPDATE", "COLLECTION_UPDATE" -> "ASSIGNMENT";
            default -> "LINE";
        };
    }

    private String description(String eventType) {
        return switch (eventType) {
            case "OUTPUT" -> "Execute output statement.";
            case "CONDITION" -> "Enter branch body.";
            case "LOOP" -> "Enter loop iteration.";
            case "ASSIGNMENT" -> "Update runtime state.";
            default -> "Execute this line.";
        };
    }

    private boolean isTraceableLine(String trimmedLine) {
        return !trimmedLine.isBlank()
                && !trimmedLine.startsWith("//")
                && !trimmedLine.startsWith("*")
                && !trimmedLine.equals("{")
                && !trimmedLine.equals("}")
                && !trimmedLine.startsWith("} else")
                && !trimmedLine.startsWith("else")
                && !trimmedLine.startsWith("catch")
                && !trimmedLine.startsWith("finally");
    }

    private boolean isMethodStart(String trimmedLine) {
        return METHOD_START_PATTERN.matcher(trimmedLine).matches()
                && !startsWithAny(trimmedLine, "if", "for", "while", "switch", "catch", "else", "do", "try")
                && !startsWithAny(trimmedLine, "class", "interface", "enum", "record");
    }

    private boolean isLoopLine(String trimmedLine) {
        return trimmedLine.startsWith("for ") || trimmedLine.startsWith("for(")
                || trimmedLine.startsWith("while ") || trimmedLine.startsWith("while(");
    }

    private boolean isConditionLine(String trimmedLine) {
        return trimmedLine.startsWith("if ") || trimmedLine.startsWith("if(")
                || trimmedLine.startsWith("else if") || trimmedLine.startsWith("else");
    }

    private boolean startsWithAny(String value, String... prefixes) {
        for (String prefix : prefixes) {
            if (value.startsWith(prefix + " ") || value.startsWith(prefix + "(")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsComparisonOperator(String line) {
        return line.contains("==") || line.contains("!=") || line.contains("<=") || line.contains(">=");
    }

    private String injectAfterOpeningBrace(String line, String statement) {
        int braceIndex = line.indexOf('{');
        return line.substring(0, braceIndex + 1) + " " + statement + line.substring(braceIndex + 1);
    }

    private List<String> splitArguments(String arguments) {
        List<String> parts = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        char quote = 0;
        int depth = 0;
        for (int index = 0; index < arguments.length(); index++) {
            char current = arguments.charAt(index);
            if (isQuoteBoundary(arguments, index, quote)) {
                quote = quote == 0 ? current : 0;
            }
            if (quote == 0) {
                if (current == '(' || current == '[' || current == '{') {
                    depth++;
                } else if (current == ')' || current == ']' || current == '}') {
                    depth = Math.max(0, depth - 1);
                } else if (current == ',' && depth == 0) {
                    parts.add(part.toString().strip());
                    part.setLength(0);
                    continue;
                }
            }
            part.append(current);
        }
        if (!part.isEmpty()) {
            parts.add(part.toString().strip());
        }
        return parts;
    }

    private String indentOf(String line) {
        int index = 0;
        while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
            index++;
        }
        return line.substring(0, index);
    }

    private int braceDelta(String line) {
        int delta = 0;
        char quote = 0;
        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (isQuoteBoundary(line, index, quote)) {
                quote = quote == 0 ? current : 0;
            }
            if (quote != 0) {
                continue;
            }
            if (current == '{') {
                delta++;
            } else if (current == '}') {
                delta--;
            }
        }
        return delta;
    }

    private boolean isQuoteBoundary(String value, int index, char quote) {
        char current = value.charAt(index);
        if (current != '"' && current != '\'' && current != '`') {
            return false;
        }
        if (index > 0 && value.charAt(index - 1) == '\\') {
            return false;
        }
        return quote == 0 || quote == current;
    }
}
