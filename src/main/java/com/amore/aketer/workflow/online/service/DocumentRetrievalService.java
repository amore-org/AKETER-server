package com.amore.aketer.workflow.online.service;

import java.util.List;

/**
 * 문서 조회 서비스 인터페이스
 */
public interface DocumentRetrievalService {

    /**
     * 메시지 내용과 관련된 윤리 강령 문서 청크들을 조회
     *
     * - 파일에서 읽어서 청크로 분할하여 반환
     * - 벡터 DB에서 유사도 검색으로 상위 K개 청크 반환
     *
     * @param messageContent 검증할 메시지 내용
     * @return 관련 윤리 강령 청크 리스트
     */
    List<String> retrieveRelevantEthicsGuidelines(String messageContent);

    /**
     * 브랜드 가이드라인 전체 조회
     *
     * - resources/documents/brand/{brandName}.txt 읽기
     * - DB 또는 벡터 스토어에서 조회
     *
     * @param brandName 브랜드명
     * @return 브랜드 톤앤매너/가이드라인 전체 텍스트
     */
    String retrieveBrandGuidelines(String brandName);
}