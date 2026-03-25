package ee.kaidokurm.ndl.ai.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiClient {

    private final RestClient rest;
    private final ObjectMapper om;
    private final Environment env;
    private final String org;
    private final String project;
    private final String apikey;

    public OpenAiClient(ObjectMapper om, Environment env) {
        this.om = om;
        this.env = env;
        this.apikey = env.getProperty("OPENAI_API_KEY");
        System.out.println(apikey);
        this.org = env.getProperty("OPENAI_ORGANIZATION");
        this.project = env.getProperty("OPENAI_PROJECT");

        // Add request/response logging interceptor
        this.rest = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .requestInterceptor((request, body, execution) -> {

                    System.out.println("\n\n===== OPENAI REQUEST =====");
                    System.out.println(request.getMethod() + " " + request.getURI());

                    request.getHeaders().forEach((k, v) -> {
                        System.out.println(k + ": " + v);
                    });

                    if (body != null) {
                        System.out.println("Body: " + new String(body, StandardCharsets.UTF_8));
                    }
                    System.out.println("==========================\n");

                    var response = execution.execute(request, body);

                    System.out.println("===== OPENAI RESPONSE =====");
                    System.out.println("Status: " + response.getStatusCode());
                    response.getHeaders().forEach((k, v) -> {
                        System.out.println(k + ": " + v);
                    });
                    System.out.println("===========================\n");

                    return response;
                })
                .build();
    }

    public String model() {
        return env.getProperty("OPENAI_MODEL", "gpt-4o-mini");
    }

    public boolean hasKey() {
        String key = env.getProperty("OPENAI_API_KEY");
        System.out.println(key);
        return key != null && !key.isBlank();
    }

    public String generateInsightJson(String systemPrompt, String userPrompt) {
        String key = env.getProperty("OPENAI_API_KEY");

        if (key == null || key.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is not set");
        }

        // Log env values
        System.out.println("=== ENV VALUES ===");
        System.out.println("API KEY: " + key.substring(0, 20) + "...");
        System.out.println("ORG: " + org);
        System.out.println("PROJECT: " + project);
        System.out.println("MODEL: " + model());
        System.out.println("==================\n");

        Map<String, Object> schema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "recommendations", Map.of(
                                "type", "array",
                                "minItems", 3,
                                "maxItems", 3,
                                "items", Map.of("type", "string", "maxLength", 220)),
                        "reflection_question", Map.of("type", "string", "maxLength", 220),
                        "summary", Map.of("type", "string", "maxLength", 400)),
                "required", java.util.List.of("recommendations", "reflection_question", "summary"));

        Map<String, Object> body = Map.of(
                "model", model(),
                "input", java.util.List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "wellness_insight_v1",
                                "strict", true,
                                "schema", schema)),
                "store", false);

        String raw = rest.post()
                .uri("/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .header("OpenAI-Organization", org)
                .header("OpenAI-Project", project)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        System.out.println("===== RAW RESPONSE BODY =====");
        System.out.println(raw);
        System.out.println("=============================\n");

        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("OpenAI response body was empty");
        }

        JsonNode resp;
        try {
            resp = om.readTree(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI response JSON", e);
        }

        JsonNode output = resp.path("output");
        for (JsonNode item : output) {
            JsonNode content = item.path("content");
            for (JsonNode c : content) {
                if ("output_text".equals(c.path("type").asText())) {
                    return c.path("text").asText();
                }
            }
        }

        throw new IllegalStateException("OpenAI response missing output_text");
    }
}

// package ee.kaidokurm.ndl.ai.openai;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import java.util.Map;

// import org.springframework.core.env.Environment;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Component;
// import org.springframework.web.client.RestClient;

// @Component
// public class OpenAiClient {

// private final RestClient rest;
// private final ObjectMapper om;
// private final Environment env;
// private final String org;
// private final String project;

// public OpenAiClient(ObjectMapper om, Environment env) {
// this.om = om;
// this.env = env;

// this.org = env.getProperty("OPENAI_ORGANIZATION");
// this.project = env.getProperty("OPENAI_PROJECT");

// this.rest = RestClient.builder()
// .baseUrl("https://api.openai.com/v1")
// .build();
// }

// public String model() {
// return env.getProperty("OPENAI_MODEL", "gpt-4o-mini");
// }

// public boolean hasKey() {
// String key = env.getProperty("OPENAI_API_KEY");
// return key != null && !key.isBlank();
// }

// public String generateInsightJson(String systemPrompt, String userPrompt) {
// String key = env.getProperty("OPENAI_API_KEY");
// if (key == null || key.isBlank()) {
// throw new IllegalStateException("OPENAI_API_KEY is not set");
// }

// // JSON Schema for strict structured output
// Map<String, Object> schema = Map.of(
// "type", "object",
// "additionalProperties", false,
// "properties", Map.of(
// "recommendations", Map.of(
// "type", "array",
// "minItems", 3,
// "maxItems", 3,
// "items", Map.of("type", "string", "maxLength", 220)),
// "reflection_question", Map.of("type", "string", "maxLength", 220),
// "summary", Map.of("type", "string", "maxLength", 400)),
// "required", java.util.List.of("recommendations", "reflection_question",
// "summary"));

// Map<String, Object> body = Map.of(
// "model", model(),
// "input", java.util.List.of(
// Map.of("role", "system", "content", systemPrompt),
// Map.of("role", "user", "content", userPrompt)),
// "text", Map.of(
// "format", Map.of(
// "type", "json_schema",
// "name", "wellness_insight_v1",
// "strict", true,
// "schema", schema)),
// "store", false);

// String raw = rest.post()
// .uri("/responses")
// .header(HttpHeaders.AUTHORIZATION, "Bearer " + key)
// .header("OpenAI-Organization", org)
// .header("OpenAI-Project", project)
// .contentType(MediaType.APPLICATION_JSON)
// .body(body)
// .retrieve()
// .body(String.class);
// System.out.println("=== OPENAI REQUEST ===");
// System.out.println("URL: https://api.openai.com/v1/responses");
// System.out.println("Authorization: Bearer " + key.substring(0, 10) + "...");
// System.out.println("OpenAI-Organization: " + org);
// System.out.println("OpenAI-Project: " + project);
// System.out.println("Model: " + model());
// // System.out.println("Body: " + om.writeValueAsString(body));
// System.out.println("======================");

// if (raw == null || raw.isBlank()) {
// throw new IllegalStateException("OpenAI response body was empty");
// }

// JsonNode resp;
// try {
// resp = om.readTree(raw);
// } catch (Exception e) {
// throw new IllegalStateException("Failed to parse OpenAI response JSON", e);
// }

// // Extract first output_text.text
// JsonNode output = resp.path("output");
// for (JsonNode item : output) {
// JsonNode content = item.path("content");
// for (JsonNode c : content) {
// if ("output_text".equals(c.path("type").asText())) {
// return c.path("text").asText();
// }
// }
// }
// throw new IllegalStateException("OpenAI response missing output_text");
// }
// }
