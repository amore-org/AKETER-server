package com.amore.aketer.external.channel;

import com.amore.aketer.domain.enums.ChannelType;
import com.amore.aketer.messaging.dto.MessagePayload;
import com.amore.aketer.messaging.dto.MessageResult;

public interface MessageChannelSender {

    MessageResult send(MessagePayload payload);

    ChannelType getChannelType();
}
