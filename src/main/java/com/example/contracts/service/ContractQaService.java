package com.example.contracts.service;

import com.example.contracts.model.Contract;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ContractQaService {

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "is", "are", "was", "were", "a", "an", "of", "in", "on", "at", "to",
            "for", "with", "by", "from", "as", "this", "that", "it", "what", "who",
            "when", "where", "which", "how", "does", "do", "can", "could", "would",
            "should", "about", "into", "be", "has", "have", "had"
    );

    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?\\n])\\s+");

    public Map<String, Object> answerQuestion(Contract contract, String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty.");
        }

        List<String> keywords = extractKeywords(question);
        String text = contract.getExtractedText();

        String[] rawSentences = SENTENCE_SPLIT.split(text);
        record ScoredSentence(String sentence, int score) {}

        List<ScoredSentence> matches = new ArrayList<>();
        for (String raw : rawSentences) {
            String cleaned = raw.trim().replaceAll("\\s+", " ");
            if (cleaned.length() < 10) continue;

            String lower = cleaned.toLowerCase(Locale.ROOT);
            int score = 0;
            for (String kw : keywords) {
                if (lower.contains(kw)) {
                    score++;
                }
            }
            if (score > 0) {
                matches.add(new ScoredSentence(cleaned, score));
            }
        }

        matches.sort((a, b) -> Integer.compare(b.score(), a.score()));

        List<String> topExcerpts = matches.stream()
                .limit(3)
                .map(ScoredSentence::sentence)
                .toList();

        String answer = topExcerpts.isEmpty()
                ? "No direct matching clauses or sentences were found in the contract for your question."
                : String.join(" ", topExcerpts);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("contractId", contract.getId());
        response.put("question", question);
        response.put("keywordsMatched", keywords);
        response.put("answer", answer);
        response.put("matchingExcerpts", topExcerpts);
        return response;
    }

    private List<String> extractKeywords(String question) {
        String[] tokens = question.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
        List<String> keywords = new ArrayList<>();
        for (String token : tokens) {
            if (token.length() > 1 && !STOP_WORDS.contains(token) && !keywords.contains(token)) {
                keywords.add(token);
            }
        }
        return keywords;
    }
}
