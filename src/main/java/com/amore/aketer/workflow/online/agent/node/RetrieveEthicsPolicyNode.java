package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.workflow.online.agent.state.MessageState;
import java.util.HashMap;
import java.util.List;
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

    private static final int TOP_K = 5;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    // ChromaDB vector store 빈 주입
    private final VectorStore vectorStore;

    @Override
    public CompletableFuture<Map<String, Object>> apply(MessageState state) {
        return CompletableFuture.supplyAsync(() -> {
            String keyword = state.getEthicsPolicyKeyword();
            Map<String, Object> updates = new HashMap<>();

            if (keyword == null || keyword.isBlank()) {
                updates.put(MessageState.VALIDATION, "fail");
                updates.put(MessageState.ETHICS_FAILURE_REASONS,
                    List.of("[시스템 오류] 윤리 강령 키워드가 생성되지 않았습니다."));
                log.warn("[RetrieveEthicsPolicyNode] 시스템 오류 - 윤리 강령 키워드 부재");
                return updates;
            }
            
            try {
                // ChromaDB 유사도 검색 설정
                SearchRequest searchRequest = SearchRequest.builder()
                    .query(keyword)
                    .topK(TOP_K)
                    .similarityThreshold(SIMILARITY_THRESHOLD)
                    .build();

                // ChromaDB에서 유사 문서 검색
                List<Document> documents = vectorStore.similaritySearch(searchRequest);

                // 검색된 문서들을 하나로 합치기
                String retrievedGuideLines = documents.stream()
                    .map(doc -> String.format("[%s]\n%s",
                        doc.getMetadata().getOrDefault("category", "윤리 규정"),
                        doc.getText()))
                    .collect(Collectors.joining("\n\n"));

                if (retrievedGuideLines.isBlank()) {
                    retrievedGuideLines = "검색된 윤리 강령이 없습니다. 일반적인 마케팅 윤리 강령을 적용하세요.";
                }

                updates.put(MessageState.ETHICS_POLICY_GUIDELINES, retrievedGuideLines);
                log.info("[RetrieveEthicsPolicyNode] 윤리 강령 검색 완료 - {}개 발견", documents.size());

            } catch (Exception e) {
                log.error("[RetrieveEthicsPolicyNode] 시스템 오류 - 윤리 강령 검색 오류");
                updates.put(MessageState.VALIDATION, "fail");
                updates.put(MessageState.ETHICS_FAILURE_REASONS,  List.of("[시스템] 윤리 강령 DB 조회 중 오류가 발생했습니다."));
            }

            return updates;
        });
    }
}
