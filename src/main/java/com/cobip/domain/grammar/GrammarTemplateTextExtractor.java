package com.cobip.domain.grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Component;

@Component
public class GrammarTemplateTextExtractor {

    public String extract(JsonNode contentJson) {
        if (contentJson == null || contentJson.isNull()) {
            return "";
        }

        List<String> texts = new ArrayList<>();
        collect(contentJson, texts);
        return texts.stream()
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining(" "));
    }

    private void collect(JsonNode node, List<String> texts) {
        if (node.isTextual()) {
            texts.add(node.asText());
            return;
        }
        if (node.isObject()) {
            node.properties().forEach(entry -> collect(entry.getValue(), texts));
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collect(child, texts));
        }
    }
}
