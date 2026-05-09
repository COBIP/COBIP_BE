package com.cobip.domain.community;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Component;

@Component
public class CommunityPostTextExtractor {

    public String extract(JsonNode contentJson) {
        if (contentJson == null || contentJson.isNull()) {
            return "";
        }

        List<String> texts = new ArrayList<>();
        collect(contentJson, texts);
        return String.join(" ", texts).replaceAll("\\s+", " ").trim();
    }

    private void collect(JsonNode node, List<String> texts) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isTextual()) {
            String text = node.asText();
            if (!text.isBlank()) {
                texts.add(text);
            }
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collect(child, texts));
            return;
        }
        if (node.isObject()) {
            node.properties().forEach(entry -> collect(entry.getValue(), texts));
        }
    }
}
