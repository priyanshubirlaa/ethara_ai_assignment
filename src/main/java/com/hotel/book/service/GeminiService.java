package com.hotel.book.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public String summarizeReviews(List<String> reviews) {

        try {
            // Combine all reviews
            String allReviews = String.join("\n", reviews);

            // ✅ Improved Prompt
            String prompt = """
                    You are an assistant that summarizes hotel reviews.

                    Instructions:
                    - Write a clear and concise summary in exactly 3 lines
                    - Avoid repetition
                    - Use simple and professional language
                    - Highlight positives first, then negatives

                    Reviews:
                    """ + allReviews;

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

            Map<String, Object> request = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            Map body = response.getBody();

            // ✅ Safety checks
            if (body == null || !body.containsKey("candidates")) {
                log.error("Invalid response from Gemini API: {}", body);
                return "Summary not available";
            }

            List candidates = (List) body.get("candidates");
            if (candidates.isEmpty()) {
                return "Summary not available";
            }

            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                return "Summary not available";
            }

            Map textPart = (Map) parts.get(0);
            String summary = textPart.get("text").toString();

            return summary.trim();

        } catch (Exception e) {
            log.error("Error while summarizing reviews", e);
            return "Error generating summary";
        }
    }
}