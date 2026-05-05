package com.cobip.domain.grammar;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

class GrammarTemplateTextExtractorTest {

    private final GrammarTemplateTextExtractor extractor = new GrammarTemplateTextExtractor();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void extractsTextValuesRecursively() throws Exception {
        var contentJson = objectMapper.readTree("""
            {
              "title": "변수 선언",
              "sections": [
                {
                  "heading": "기본 문법",
                  "body": "int count = 1;"
                },
                {
                  "examples": ["System.out.println(count);"]
                }
              ]
            }
            """);

        String searchableText = extractor.extract(contentJson);

        assertThat(searchableText).isEqualTo("변수 선언 기본 문법 int count = 1; System.out.println(count);");
    }

    @Test
    void ignoresJsonKeysBlankTextAndNonTextValues() throws Exception {
        var contentJson = objectMapper.readTree("""
            {
              "heading": " ",
              "count": 3,
              "enabled": true,
              "body": "반복문"
            }
            """);

        String searchableText = extractor.extract(contentJson);

        assertThat(searchableText).isEqualTo("반복문");
        assertThat(searchableText).doesNotContain("heading");
    }
}
