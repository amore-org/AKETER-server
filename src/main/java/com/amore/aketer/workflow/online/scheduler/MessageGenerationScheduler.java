package com.amore.aketer.workflow.online.scheduler;

import com.amore.aketer.domain.association.PersonaItem;
import com.amore.aketer.domain.association.PersonaItemRepository;
import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.domain.enums.MessageStatus;
import com.amore.aketer.domain.enums.RecommendTargetType;
import com.amore.aketer.domain.item.Item;
import com.amore.aketer.domain.message.Message;
import com.amore.aketer.domain.message.MessageRepository;
import com.amore.aketer.domain.message.MessageReservation;
import com.amore.aketer.domain.message.MessageReservationRepository;
import com.amore.aketer.domain.order.OrderRepository;
import com.amore.aketer.domain.persona.Persona;
import com.amore.aketer.domain.persona.PersonaRepository;
import com.amore.aketer.domain.recommend.Recommend;
import com.amore.aketer.domain.recommend.RecommendRepository;
import com.amore.aketer.domain.user.User;
import com.amore.aketer.domain.user.UserRepository;
import com.amore.aketer.workflow.online.agent.graph.MessageGraph;
import com.amore.aketer.workflow.online.agent.state.ItemState;
import com.amore.aketer.workflow.online.agent.state.MessageState;
import com.amore.aketer.workflow.online.agent.state.PersonaState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageGenerationScheduler {

    private final PersonaRepository personaRepository;
    private final PersonaItemRepository personaItemRepository;
    private final MessageRepository messageRepository;
    private final MessageReservationRepository messageReservationRepository;
    private final UserRepository userRepository;
    private final RecommendRepository recommendRepository;
    private final MessageGraph messageGraph;

    @Scheduled(cron = "0 0 7 * * *") // 매일 아침 7시 실행
    @Transactional
    public void scheduleDailyMessageGeneration() {
        log.info("Starting daily message generation batch.");

        // 오늘 00:00:00 이후 생성된 페르소나 조회
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<Persona> newPersonas = personaRepository.findByCreatedAtAfter(startOfDay);

        log.info("Found {} new personas created since {}", newPersonas.size(), startOfDay);

        // 페르소나 별 작업 시작
        for (Persona persona : newPersonas) {
            // 1. 해당 페르소나의 추천 상품 목록 조회 (Rank 순)
            List<PersonaItem> personaItems = personaItemRepository.findByPersonaIdOrderByRankAsc(persona.getId());
            if (personaItems.isEmpty()) {
                continue;
            }

            // [Phase 1] 상품별 메시지 생성 및 Recommend 저장
            List<Recommend> generatedRecommends = new ArrayList<>();

            for (PersonaItem personaItem : personaItems) {
                try {
                    Item item = personaItem.getItem();

                    // LangGraph 실행
                    Map<String, Object> initData = buildInitData(persona, item);
                    MessageState result = messageGraph.execute(initData).join();

                    // 검증 실패 시 스킵
                    if (!"pass".equalsIgnoreCase(result.getValidation())) {
                        log.warn("Validation failed for Persona ID: {}, Item ID: {}. Reasons: {}",
                                persona.getId(), item.getId(), result.getMessageFailureReasons());
                        continue;
                    }

                    // Message 엔티티 저장
                    Message message = Message.builder()
                            .title(result.getMessageTitle())
                            .body(result.getMessageBody())
                            .build();
                    messageRepository.save(message);

                    // 발송 정보 추출 (MessageState에서 ChannelType 직접 사용)
                    ChannelType channelType = result.getChannel();
                    if (channelType == null) {
                        channelType = ChannelType.SMS; // 기본값
                    }
                    
                    LocalDateTime scheduledAt = result.getSendTime();
                    if (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now())) {
                        scheduledAt = LocalDateTime.now().plusHours(1);
                    }

                    // Recommend 엔티티 생성 및 저장
                    Recommend recommend = Recommend.builder()
                            .targetId(persona.getId())
                            .targetType(RecommendTargetType.PERSONA)
                            .item(item)
                            .message(message)
                            .recommendReason(result.getDraftReason())
                            .channelType(channelType)
                            .scheduledAt(scheduledAt)
                            .build();
                    recommendRepository.save(recommend);
                    
                    generatedRecommends.add(recommend);

                } catch (Exception e) {
                    log.error("Failed to generate message for persona {} and item {}", persona.getId(), personaItem.getItem().getId(), e);
                }
            }

            if (generatedRecommends.isEmpty()) {
                log.info("No messages generated for Persona ID: {}", persona.getId());
                continue;
            }

            // [Phase 2] 유저별 미구매 상품 매칭 및 예약
            List<User> users = userRepository.findByPersonaId(persona.getId());
            if (users.isEmpty()) {
                continue;
            }

            // 생성된 Recommend ID 목록 추출 (쿼리용)
            List<Long> recommendIds = generatedRecommends.stream()
                    .map(Recommend::getId)
                    .toList();

            List<MessageReservation> newReservations = new ArrayList<>();

            for (User user : users) {
                // DB 쿼리로 유저가 구매하지 않은, 가장 우선순위 높은 Recommend 조회
                recommendRepository.findFirstValidRecommend(user.getId(), recommendIds)
                        .ifPresent(recommend -> {
                            // 채널 주소 확인
                            ChannelType channelType = recommend.getChannelType();
                            String address = getChannelAddress(user, channelType);

                            if (StringUtils.hasText(address)) {
                                // 예약 생성
                                MessageReservation reservation = MessageReservation.builder()
                                        .persona(persona)
                                        .user(user)
                                        .message(recommend.getMessage())
                                        .item(recommend.getItem())
                                        .recommendReason(recommend.getRecommendReason())
                                        .channelType(channelType)
                                        .channelAddress(address)
                                        .status(MessageStatus.READY)
                                        .scheduledAt(recommend.getScheduledAt())
                                        .build();
                                newReservations.add(reservation);
                            }
                        });
            }

            if (!newReservations.isEmpty()) {
                messageReservationRepository.saveAll(newReservations);
                log.info("Created {} reservations for Persona ID: {}", newReservations.size(), persona.getId());
            }
        }

        log.info("Daily message generation batch completed.");
    }

    private String getChannelAddress(User user, ChannelType channelType) {
        if (channelType == null) return null;
        return switch (channelType) {
            case KAKAO -> user.getKakaoEmail();
            case SMS -> user.getPhoneNumber();
        };
    }

    private Map<String, Object> buildInitData(Persona persona, Item item) {
        Map<String, Object> initData = new HashMap<>();
        initData.put(MessageState.PERSONA, PersonaState.from(persona));
        initData.put(MessageState.ITEM, ItemState.from(item));

        return initData;
    }
}