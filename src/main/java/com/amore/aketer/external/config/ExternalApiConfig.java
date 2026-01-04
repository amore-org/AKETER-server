package com.amore.aketer.external.config;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.external.channel.MessageChannelSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class ExternalApiConfig {

    @Bean
    public Map<ChannelType, MessageChannelSender> channelSenderMap(List<MessageChannelSender> senders) {
        return senders.stream()
                .collect(Collectors.toMap(
                        MessageChannelSender::getChannelType,
                        sender -> sender
                ));
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
