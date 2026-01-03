package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.workflow.online.agent.state.MessageState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetrieveEthicsPolicyNode implements AsyncNodeAction<MessageState> {

    private static final int TOP_K = 3;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    private final VectorStore vectorStore;

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        return CompletableFuture.supplyAsync(() -> {
            String ethicKeywords = state.getEthicsPolicyKeyword();
            Map<String, Object> updates = new HashMap<>();

            if (ethicKeywords == null || ethicKeywords.isBlank()) {
                updates.put(MessageState.VALIDATION, "fail");
                updates.put(MessageState.ETHICS_FAILURE_REASONS,
                    List.of("[시스템 오류] 윤리 강령 키워드가 생성되지 않았습니다."));
                log.warn("[RetrieveEthicsPolicyNode] 시스템 오류 - 윤리 강령 키워드 부재");
                return updates;
            }

            try {
                String[] keywords = ethicKeywords.split(",");

                // 각 키워드별 검색
                List<CompletableFuture<List<Document>>> futures = Arrays.stream(keywords)
                    .map(String::trim)
                    .filter(keyword -> !keyword.isBlank())
                    .map(keyword -> CompletableFuture.supplyAsync(() -> {
                        SearchRequest request = SearchRequest.builder()
                            .query(keyword)
                            .topK(TOP_K)
                            .similarityThreshold(SIMILARITY_THRESHOLD)
                            .build();
                        return vectorStore.similaritySearch(request);
                    }))
                    .toList();

                // 결과 병합
                Set<Document> mergedDocuments = futures.stream()
                    .flatMap(future -> future.join().stream())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

                List<Document> documents = new ArrayList<>(mergedDocuments);

                String retrievedGuideLines = documents.stream()
                    .map(doc -> String.format("[%s]\n%s",
                        doc.getMetadata().getOrDefault("category", "윤리 강령"),
                        doc.getText()))
                    .collect(Collectors.joining("\n\n"));

                if (retrievedGuideLines.isBlank()) {
                    retrievedGuideLines = "검색된 윤리 강령이 없습니다. 일반적인 마케팅 윤리 강령을 적용하세요.";
                }

                updates.put(MessageState.ETHICS_POLICY_GUIDELINES, retrievedGuideLines);
                log.info("[RetrieveEthicsPolicyNode] 윤리 강령 검색 완료 - {}개 발견", documents.size());

            } catch (Exception e) {
                log.error("[RetrieveEthicsPolicyNode] 시스템 오류 - 윤리 강령 검색 오류: {}", e.getMessage());
                updates.put(MessageState.VALIDATION, "fail");
                updates.put(MessageState.ETHICS_FAILURE_REASONS, List.of("[시스템] 윤리 강령 DB 조회 중 오류가 발생했습니다."));
            }

            return updates;
        });
    }
}
