package com.amore.aketer.workflow.online.agent.node;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class RetrieveEthicsPolicyNodeTest {

    @Autowired
    private VectorStore vectorStore;

    @Test
    void testVectorStoreConnection() {
        log.info("=== VectorStore 연결 테스트 시작 ===");

        try {
            SearchRequest request = SearchRequest.builder()
                .query("개인정보")
                .topK(5)
                .similarityThreshold(0.3)
                .build();

            List<Document> results = vectorStore.similaritySearch(request);

            log.info("검색 결과 개수: {}", results.size());
            for (Document doc : results) {
                log.info("문서: {}", doc.getText());
                log.info("메타데이터: {}", doc.getMetadata());
            }
        } catch (Exception e) {
            log.error("VectorStore 검색 실패: {}", e.getMessage(), e);
        }
    }

    @Test
    void testAddAndSearch() {
        log.info("=== 데이터 추가 및 검색 테스트 ===");

        try {
            List<Document> documents = List.of(
                new Document("테스트 윤리 강령: 고객 정보를 보호해야 한다.", Map.of("category", "테스트"))
            );

            vectorStore.add(documents);
            log.info("문서 추가 완료");

            SearchRequest request = SearchRequest.builder()
                .query("고객 정보 보호")
                .topK(3)
                .build();

            List<Document> results = vectorStore.similaritySearch(request);
            log.info("검색 결과: {}", results.size());

            for (Document doc : results) {
                log.info("결과: {}", doc.getText());
            }
        } catch (Exception e) {
            log.error("테스트 실패: {}", e.getMessage(), e);
        }
    }
}
