package org.example.cloud.services;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.example.cloud.dto.BasicMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final SqsTemplate messagingTemplate;


    public void send(BasicMessage message) {
        messagingTemplate.send(sqs -> sqs.queue("messages_to_vk.fifo").payload(message));
    }

}
