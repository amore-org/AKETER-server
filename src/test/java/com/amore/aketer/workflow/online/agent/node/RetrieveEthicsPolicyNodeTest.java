package com.amore.aketer.workflow.online.agent.node;

import com.amore.aketer.workflow.online.agent.state.MessageState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RetrieveEthicsPolicyNodeTest {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private RetrieveEthicsPolicyNode retrieveEthicsPolicyNode;

    @BeforeAll
    void initTestData() {
        log.info("=== 테스트 데이터 초기화 ===");

        List<Document> documents = List.of(
            new Document("고객의 개인정보는 수집 목적 외에 사용하지 않는다.", Map.of("category", "개인정보")),
            new Document("허위 또는 과장된 광고 표현을 사용하지 않는다.", Map.of("category", "공정거래")),
            new Document("타사 제품을 비방하거나 근거 없이 비교하지 않는다.", Map.of("category", "윤리경영")),
            new Document("의약품으로 오인될 수 있는 치료, 질병 예방 등의 표현을 사용하지 않는다.", Map.of("category", "화장품법")),
            new Document("소비자를 오인하게 하는 표현이나 경쟁사 비방을 금지한다.", Map.of("category", "소비자보호"))
        );

        try {
            vectorStore.add(documents);
            log.info("테스트 데이터 {}건 추가 완료", documents.size());
        } catch (Exception e) {
            log.warn("테스트 데이터 추가 실패: {}", e.getMessage());
        }
    }

    @Test
    @DisplayName("VectorStore 연결 테스트")
    void testVectorStoreConnection() {
        log.info("=== VectorStore 연결 테스트 시작 ===");

        try {
            SearchRequest request = SearchRequest.builder()
                .query("개인정보")
                .topK(3)
                .similarityThreshold(0.3)
                .build();

            List<Document> results = vectorStore.similaritySearch(request);

            log.info("검색 결과 개수: {}", results.size());
            for (Document doc : results) {
                log.info("문서: {}", doc.getText());
                log.info("메타데이터: {}", doc.getMetadata());
            }

            assertThat(results).isNotNull();
        } catch (Exception e) {
            log.error("VectorStore 검색 실패: {}", e.getMessage(), e);
        }
    }

    @Test
    @DisplayName("단일 키워드 검색 테스트")
    void testSingleKeywordSearch() throws ExecutionException, InterruptedException {
        log.info("=== 단일 키워드 검색 테스트 ===");

        Map<String, Object> initData = new HashMap<>();
        initData.put(MessageState.ETHICS_POLICY_KEYWORD, "개인정보 보호");

        MessageState state = new MessageState(initData);
        Map<String, Object> result = retrieveEthicsPolicyNode.apply(state).get();

        log.info("검색 결과: {}", result.get(MessageState.ETHICS_POLICY_GUIDELINES));

        assertThat(result).containsKey(MessageState.ETHICS_POLICY_GUIDELINES);
        assertThat(result.get(MessageState.VALIDATION)).isNull();
    }

    @Test
    @DisplayName("다중 키워드 검색 테스트")
    void testMultipleKeywordsParallelSearch() throws ExecutionException, InterruptedException {
        log.info("=== 다중 키워드 검색 테스트 ===");

        Map<String, Object> initData = new HashMap<>();
        initData.put(MessageState.ETHICS_POLICY_KEYWORD, "개인정보 보호랑 과대광고랑 허위표시 조회해줘");

        MessageState state = new MessageState(initData);

        long startTime = System.currentTimeMillis();
        Map<String, Object> result = retrieveEthicsPolicyNode.apply(state).get();
        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("검색 소요 시간: {}ms", elapsedTime);
        log.info("검색 결과: {}", result.get(MessageState.ETHICS_POLICY_GUIDELINES));

        assertThat(result).containsKey(MessageState.ETHICS_POLICY_GUIDELINES);
    }

    @Test
    @DisplayName("다중 키워드 검색 성능 테스트")
    void testManyKeywordsPerformance() throws ExecutionException, InterruptedException {
        log.info("=== 다중 키워드 성능 테스트 ===");

        Map<String, Object> initData = new HashMap<>();
        initData.put(MessageState.ETHICS_POLICY_KEYWORD,
            "개인정보, 과대광고, 허위표시, 공정거래, 화장품법, 소비자보호, 윤리경영");

        MessageState state = new MessageState(initData);

        long startTime = System.currentTimeMillis();
        Map<String, Object> result = retrieveEthicsPolicyNode.apply(state).get();
        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("7개 키워드 검색 소요 시간: {}ms", elapsedTime);
        log.info("검색 결과 길이: {} chars",
            result.get(MessageState.ETHICS_POLICY_GUIDELINES).toString().length());

        assertThat(result).containsKey(MessageState.ETHICS_POLICY_GUIDELINES);
    }

    @Test
    @DisplayName("키워드 없음 - 실패 처리 테스트")
    void testNullKeyword() throws ExecutionException, InterruptedException {
        log.info("=== 키워드 없음 테스트 ===");

        Map<String, Object> initData = new HashMap<>();
        // ETHICS_POLICY_KEYWORD를 설정하지 않음

        MessageState state = new MessageState(initData);
        Map<String, Object> result = retrieveEthicsPolicyNode.apply(state).get();

        log.info("결과: {}", result);

        assertThat(result.get(MessageState.VALIDATION)).isEqualTo("fail");
        assertThat(result.get(MessageState.ETHICS_FAILURE_REASONS)).isNotNull();
    }

    @Test
    @DisplayName("빈 키워드 - 실패 처리 테스트")
    void testBlankKeyword() throws ExecutionException, InterruptedException {
        log.info("=== 빈 키워드 테스트 ===");

        Map<String, Object> initData = new HashMap<>();
        initData.put(MessageState.ETHICS_POLICY_KEYWORD, "   ");

        MessageState state = new MessageState(initData);
        Map<String, Object> result = retrieveEthicsPolicyNode.apply(state).get();

        assertThat(result.get(MessageState.VALIDATION)).isEqualTo("fail");
    }

    @Test
    @DisplayName("초기화 데이터 검색 테스트")
    void testSearchInitializedData() {
        log.info("=== 초기화 데이터 검색 테스트 ===");

        SearchRequest request = SearchRequest.builder()
            .query("개인정보")
            .topK(3)
            .similarityThreshold(0.3)
            .build();

        List<Document> results = vectorStore.similaritySearch(request);
        log.info("검색 결과: {}", results.size());

        for (Document doc : results) {
            log.info("결과: {}", doc.getText());
            log.info("카테고리: {}", doc.getMetadata().get("category"));
        }

        assertThat(results).isNotEmpty();
    }
}
