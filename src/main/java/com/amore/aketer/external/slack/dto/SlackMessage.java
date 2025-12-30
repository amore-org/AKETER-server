package com.amore.aketer.external.slack.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SlackMessage {

    private String text;
    private List<Attachment> attachments;

    @Getter
    @Builder
    public static class Attachment {
        private String color;
        private String title;
        private String text;
        private List<Field> fields;
    }

    @Getter
    @Builder
    public static class Field {
        private String title;
        private String value;
        @Builder.Default
        private boolean shortField = true;
    }
}
