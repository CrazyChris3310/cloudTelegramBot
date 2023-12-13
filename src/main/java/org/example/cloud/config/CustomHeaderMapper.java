package org.example.cloud.config;

import io.awspring.cloud.sqs.support.converter.SqsHeaderMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.MessageHeaders;
import software.amazon.awssdk.services.sqs.model.Message;

public class CustomHeaderMapper extends SqsHeaderMapper {


    @NotNull
    @Override
    public MessageHeaders toHeaders(Message source) {
        String invalidUUID = source.messageId();
        String validUUID = formatUUID(invalidUUID);
        source = source.toBuilder().messageId(validUUID).build();
        return super.toHeaders(source);
    }

    public String formatUUID(String inputUUID) {
        String cleanedUUID = inputUUID.replace("-", "");

        return String.format(
                "%s-%s-%s-%s-%s",
                cleanedUUID.substring(0, 8),
                cleanedUUID.substring(8, 12),
                cleanedUUID.substring(12, 16),
                cleanedUUID.substring(16, 20),
                cleanedUUID.substring(20)
        );
    }
}